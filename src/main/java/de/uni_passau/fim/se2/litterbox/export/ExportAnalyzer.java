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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

/**
 * Analyzer for exporting Scratch projects to various text-based formats.
 *
 * <p>Supports exporting to:
 * <ul>
 *   <li>ScratchBlocks - visual block notation</li>
 *   <li>Goboscript - C/Rust-style text language</li>
 *   <li>Leila - intermediate language for analysis</li>
 * </ul>
 */
public class ExportAnalyzer extends FileAnalyzer<String> {

    private static final Logger log = Logger.getLogger(ExportAnalyzer.class.getName());

    private final ExportFormat format;
    private final boolean nonDet;
    private BufferedWriter writer;

    /**
     * Creates an export analyzer with the specified format.
     *
     * @param format The export format to use
     * @param output Path to output file or directory
     * @param delete Whether to delete project files after analysis
     */
    public ExportAnalyzer(ExportFormat format, Path output, boolean delete) {
        this(format, output, delete, false);
    }

    /**
     * Creates an export analyzer with full configuration.
     *
     * @param format The export format to use
     * @param output Path to output file or directory
     * @param delete Whether to delete project files after analysis
     * @param nonDet For Leila: allow non-deterministic attributes
     */
    public ExportAnalyzer(ExportFormat format, Path output, boolean delete, boolean nonDet) {
        super(new ExportProgramAnalyzer(format), output, delete);
        this.format = format;
        this.nonDet = nonDet;
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
        } else if (format == ExportFormat.SCRATCHBLOCKS && !Files.isDirectory(output)) {
            // ScratchBlocks to single file
            writeScratchBlocksResult(result);
        } else if (Files.isDirectory(output)) {
            // Directory output: write each project to separate file
            writeToSeparateFile(projectFile, program, result);
        } else {
            log.warning("Output path must be a directory for multiple projects with " + format + " format");
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
                case LEILA -> {
                    // Leila output is handled differently in writeResultToFile
                    // Return empty string here as a placeholder
                    yield "";
                }
            };
        }
    }
}
