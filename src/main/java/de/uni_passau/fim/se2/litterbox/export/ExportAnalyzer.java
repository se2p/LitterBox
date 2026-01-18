/*
 * Copyright (C) 2019-2026 LitterBox contributors
 *
 * This file is part of LitterBox.
 *
 * LitterBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * LitterBox is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LitterBox. If not, see <http://www.gnu.org/licenses/>.
 */
package de.uni_passau.fim.se2.litterbox.export;

import de.uni_passau.fim.se2.litterbox.analytics.FileAnalyzer;
import de.uni_passau.fim.se2.litterbox.analytics.ProgramAnalyzer;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.export.leila.LeilaVisitor;
import de.uni_passau.fim.se2.litterbox.export.scratchblocks.ScratchBlocksVisitor;
import de.uni_passau.fim.se2.litterbox.export.goboscript.GoboscriptVisitor;
import de.uni_passau.fim.se2.litterbox.export.goboscript.GoboscriptProjectExporter;
import de.uni_passau.fim.se2.litterbox.export.pystage.PyStageVisitor;
import de.uni_passau.fim.se2.litterbox.export.pystage.PyStageProjectExporter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

import java.util.stream.Stream;

/**
 * Analyzer for exporting Scratch projects to various text-based formats.
 *
 * <p>Supports exporting to:
 * <ul>
 *   <li>ScratchBlocks - visual block notation</li>
 *   <li>Goboscript - C/Rust-style text language</li>
 *   <li>Leila - intermediate language for analysis</li>
 *   <li>PyStage - Python code using PyStage library</li>
 * </ul>
 */
public class ExportAnalyzer extends FileAnalyzer<String> {

    private static final Logger log = Logger.getLogger(ExportAnalyzer.class.getName());

    private final ExportFormat format;
    private final boolean nonDet;
    private final boolean fullProject;
    private BufferedWriter writer;
    private int exportedCount = 0;

    /**
     * Creates an export analyzer with the specified format.
     *
     * @param format The export format to use
     * @param output Path to output file or directory
     * @param delete Whether to delete project files after analysis
     */
    public ExportAnalyzer(ExportFormat format, Path output, boolean delete) {
        this(format, output, delete, false, false);
    }

    /**
     * Creates an export analyzer with full configuration.
     *
     * @param format The export format to use
     * @param output Path to output file or directory
     * @param delete Whether to delete project files after analysis
     * @param nonDet For Leila: allow non-deterministic attributes
     * @param fullProject For Goboscript: create full project structure with separate files and assets
     */
    public ExportAnalyzer(ExportFormat format, Path output, boolean delete, boolean nonDet, boolean fullProject) {
        super(new ExportProgramAnalyzer(format), output, delete);
        this.format = format;
        this.nonDet = nonDet;
        this.fullProject = fullProject;
    }

    @Override
    public void analyzeFile(final Path input) throws IOException {
        // If input is a directory, check if it contains multiple Scratch projects.
        // If so, and we are exporting to a single file (and format is not ScratchBlocks which supports merging),
        // we should error immediately.
        if (Files.isDirectory(input) && output != null && !Files.isDirectory(output) && format != ExportFormat.SCRATCHBLOCKS) {
            try (Stream<Path> stream = Files.list(input)) {
                long count = stream.filter(p -> {
                    String name = p.getFileName().toString().toLowerCase();
                    return name.endsWith(".sb3") || name.endsWith(".json");
                }).limit(2).count();
                if (count > 1) {
                    throw new IOException("Cannot export multiple projects (from directory) to a single file with format " + format + ". Output must be a directory.");
                }
            }
        }
        super.analyzeFile(input);
    }

    @Override
    protected void beginAnalysis() throws IOException {
        if (format == ExportFormat.SCRATCHBLOCKS && output != null && !Files.isDirectory(output)) {
            // ScratchBlocks writes all projects to a single file
            writer = Files.newBufferedWriter(output);
        }
    }

    @Override
    protected void endAnalysis() throws IOException {
        if (writer != null) {
            writer.close();
        }
    }

    @Override
    protected void writeResultToFile(Path projectFile, Program program, String result) throws IOException {
        if (output == null) {
            // No output path: print to stdout for all formats
            writeToStdout(program, result);
            return;
        }

        // Handle --full-project: requires directory structure
        if (fullProject && (format == ExportFormat.GOBOSCRIPT || format == ExportFormat.PYSTAGE)) {
            writeFullProject(projectFile, program);
            return;
        }

        // Handle standard export
        if (Files.isDirectory(output)) {
            // Directory output: write each project to separate file
            writeToSeparateFile(projectFile, program, result);
        } else {
            // File output
            if (format == ExportFormat.SCRATCHBLOCKS) {
                // ScratchBlocks appends to the single file (writer opened in beginAnalysis)
                writeScratchBlocksResult(result);
            } else {
                if (exportedCount > 0) {
                     throw new IOException("Cannot export multiple projects to a single file with format " + format + ". Output must be a directory.");
                }
                // Other formats overwrite the file if single input, but error if multiple inputs (checked via exportedCount)
                writeSingleFile(output, program, result);
            }
        }
        exportedCount++;
    }

    private void writeSingleFile(Path target, Program program, String result) throws IOException {
        if (format == ExportFormat.LEILA) {
            try (PrintStream stream = new PrintStream(target.toFile(), StandardCharsets.UTF_8)) {
                LeilaVisitor visitor = new LeilaVisitor(stream, nonDet, false);
                visitor.visit(program);
            }
        } else {
            Files.writeString(target, result, StandardCharsets.UTF_8);
        }
    }

    private void writeFullProject(Path projectFile, Program program) throws IOException {
        String fileName = projectFile.getFileName().toString();
        if (!fileName.toLowerCase().endsWith(".sb3")) {
            log.severe("Full project export (--full-project) requires .sb3 input file. "
                    + "JSON files do not contain the required assets (costumes, sounds). "
                    + "Input file: " + fileName);
            return;
        }

        // Output must be a directory for full project export
        if (Files.exists(output) && !Files.isDirectory(output)) {
            log.severe("Output path must be a directory (or non-existent path to be created as directory) "
                    + "for --full-project export.");
            return;
        }

        // Determine project directory:
        // - If output doesn't exist: use it directly as the project directory
        // - If output exists as a directory: create a subdirectory with the project name
        Path projectDir;
        if (Files.exists(output)) {
            String baseName = fileName.substring(0, fileName.lastIndexOf("."));
            projectDir = output.resolve(baseName);
        } else {
            projectDir = output;
        }

        // Ensure the directory exists
        Files.createDirectories(projectDir);

        if (format == ExportFormat.GOBOSCRIPT) {
            new GoboscriptProjectExporter().export(projectFile, program, projectDir);
        } else if (format == ExportFormat.PYSTAGE) {
            new PyStageProjectExporter().export(projectFile, program, projectDir);
        }
    }

    private void writeToStdout(Program program, String result) {
        if (format == ExportFormat.LEILA) {
            // Leila needs its visitor with PrintStream
            LeilaVisitor visitor = new LeilaVisitor(System.out, nonDet, false);
            visitor.visit(program);
            System.out.println();
        } else {
            // ScratchBlocks and Goboscript use pre-computed result
            System.out.println(result);
        }
    }

    private void writeScratchBlocksResult(String result) throws IOException {
        writer.write(result);
        writer.newLine();
    }

    private void writeToSeparateFile(Path projectFile, Program program, String result) throws IOException {
        String outName = getOutputFileName(projectFile.getFileName().toString());
        Path outPath = output.resolve(outName);

        if (format == ExportFormat.LEILA) {
            // Leila uses its visitor directly for output
            try (PrintStream stream = new PrintStream(outPath.toString(), StandardCharsets.UTF_8)) {
                log.info("Starting to print " + projectFile.getFileName() + " to file " + outPath);
                LeilaVisitor visitor = new LeilaVisitor(stream, nonDet, false);
                visitor.visit(program);
                log.info("Finished printing.");
            }
        } else {
            // Other formats use the pre-computed result string
            Files.writeString(outPath, result, StandardCharsets.UTF_8);
        }
    }

    private String getOutputFileName(String name) {
        String baseName = name.substring(0, name.lastIndexOf("."));
        return baseName + format.getExtension();
    }

    /**
     * Internal program analyzer that delegates to the appropriate visitor.
     */
    private static class ExportProgramAnalyzer implements ProgramAnalyzer<String> {

        private final ExportFormat format;

        ExportProgramAnalyzer(ExportFormat format) {
            this.format = format;
        }

        @Override
        public String analyze(Program program) {
            return switch (format) {
                case SCRATCHBLOCKS -> {
                    ScratchBlocksVisitor vis = new ScratchBlocksVisitor();
                    vis.setAddActorNames(true);
                    program.accept(vis);
                    yield vis.getScratchBlocks();
                }
                case GOBOSCRIPT -> GoboscriptVisitor.of(program);
                case PYSTAGE -> PyStageVisitor.of(program);
                case LEILA -> {
                    // Leila output is handled differently in writeResultToFile
                    // Return empty string here as a placeholder
                    yield "";
                }
            };
        }
    }
}
