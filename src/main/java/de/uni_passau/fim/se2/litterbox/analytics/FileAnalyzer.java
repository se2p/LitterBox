/*
 * Copyright (C) 2019-2024 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.analytics;

import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.new_parser.NewParser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;

public abstract class FileAnalyzer<R> {

    private static final Logger log = Logger.getLogger(FileAnalyzer.class.getName());

    private final NewParser parser = new NewParser();

    protected final ProgramAnalyzer<R> analyzer;
    protected final Path output;
    protected final boolean delete;

    public FileAnalyzer(final ProgramAnalyzer<R> analyzer, final Path output, boolean delete) {
        this.analyzer = analyzer;
        this.output = output;
        this.delete = delete;
    }

    /**
     * Analyzes the file or directory this analyzer was initialized with.
     *
     * <p>If the input is a file it will be directly analyzed, if it is a director all files in the
     * directory will be analyzed one after another.</p>
     */
    public final void analyzeFile(final Path input) throws IOException {
        File file = input.toFile();
        if (file.exists() && file.isDirectory()) {
            List<Path> listOfFiles = getProgramPaths(file.toPath());
            for (Path filePath : listOfFiles) {
                File fileEntry = filePath.toFile();
                checkAndWrite(fileEntry);
                deleteFile(fileEntry);
            }
        } else if (file.exists() && !file.isDirectory()) {
            checkAndWrite(file);
            deleteFile(file);
        } else {
            log.severe("Folder or file '" + file.getName() + "' does not exist");
        }
    }

    private static List<Path> getProgramPaths(Path dirPath) throws IOException {
        try (var files = Files.walk(dirPath, 1)) {
            return files.filter(p -> !Files.isDirectory(p))
                    .filter(FileAnalyzer::isPossibleScratchFile)
                    .toList();
        }
    }

    private void deleteFile(File file) {
        if (delete && isPossibleScratchFile(file.toPath())) {
            boolean success = file.delete();
            if (!success) {
                log.warning("Could not delete project: " + file.getName());
            }
        }
    }

    private static boolean isPossibleScratchFile(final Path path) {
        final String filename = path.getFileName().toString();
        return filename.endsWith(".json") || filename.endsWith(".sb3");
    }

    /**
     * First uses {@link ProgramAnalyzer#analyze(Program)} to parse and check the program, then writes the result to the
     * output file.
     *
     * <p>In case a subclass can more efficiently combine the two operations {@link ProgramAnalyzer#analyze(Program)}
     * and {@link #writeResultToFile(Path, Program, Object)}, e.g., to avoid (partial) recalculation of the check
     * results, it should overwrite this method.
     *
     * @param file The input file that contains the Scratch program.
     * @throws IOException In case either reading the program fails, or writing to the output file fails.
     */
    protected void checkAndWrite(final File file) throws IOException {
        final Program program = extractProgram(file);
        if (program == null) {
            return;
        }

        final R result = analyzer.analyze(program);
        writeResultToFile(file.toPath(), program, result);
    }

    /**
     * Writes the analysis result to the output destination {@link #output}.
     *
     * @param projectFile The path of the sb3 or json file from which the program was originally read.
     * @param program The analysed program.
     * @param checkResult The analysis result.
     * @throws IOException Thrown in case writing to the destination fails.
     */
    protected abstract void writeResultToFile(Path projectFile, Program program, R checkResult) throws IOException;

    /**
     * Extracts a Scratch Program from a Json or sb3 file.
     *
     * @param fileEntry of the json or sb3 file
     * @return the parsed program or null in case the program could not be loaded or parsed
     */
    protected final Program extractProgram(File fileEntry) {
        try {
            return parser.parseFile(fileEntry);
        } catch (ParsingException e) {
            log.severe("Could not parse program for file " + fileEntry.getName() + ". " + e.getMessage());
        } catch (RuntimeException e) {
            log.severe("Could not parse program for file " + fileEntry.getName() + ".");
        }

        return null;
    }
}
