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
package de.uni_passau.fim.se2.embedded_kittens;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.parser.Scratch3Parser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Stream;

public abstract class MLFilePreprocessor<R> {

    private static final Logger log = Logger.getLogger(MLFilePreprocessor.class.getName());

    private static final Scratch3Parser PARSER = new Scratch3Parser();

    protected final MLProgramPreprocessor<R> programPreprocessor;
    private final MLOutputPath outputPath;

    protected MLFilePreprocessor(final MLProgramPreprocessor<R> programPreprocessor, final MLOutputPath outputPath) {
        this.programPreprocessor = programPreprocessor;
        this.outputPath = outputPath;
    }

    public MLProgramPreprocessor<R> getProgramPreprocessor() {
        return programPreprocessor;
    }

    /**
     * Determines the output file name based on the input file name.
     *
     * <p>
     * Should receive and return a file name rather than a path with multiple elements.
     *
     * @param inputFile The file name of the input file.
     * @return The output file name.
     */
    protected abstract Path outputFileName(Path inputFile);

    /**
     * Processes either a single file, or the input as directory recursively.
     *
     * @param input A file or directory.
     */
    public void processProgram(final Path input) {
        process(input, true);
    }

    /**
     * Processes either a single file, or the input as directory recursively.
     *
     * @param input A file or directory.
     */
    public void processPerSprite(final Path input) {
        process(input, false);
    }

    private void process(final Path input, final boolean wholeProgram) {
        final File inputFile = input.toFile();
        if (!inputFile.exists()) {
            log.warning("Input file '" + input + "' does not exist!");
            return;
        }

        if (input.toFile().isFile()) {
            processFile(input.getParent(), input, wholeProgram);
        } else if (input.toFile().isDirectory()) {
            processDirectory(input, wholeProgram);
        }
    }

    private void processFile(final Path inputBaseDir, final Path programPath, final boolean wholeProgram) {
        try {
            final Stream<R> results = readProgram(programPath).stream().flatMap(program -> {
                if (wholeProgram) {
                    return programPreprocessor.processWholeProgram(program);
                } else {
                    return programPreprocessor.processSprites(program);
                }
            });
            writeResultToOutput(inputBaseDir.relativize(programPath), results);
        } catch (IOException e) {
            log.warning("Could not process file '" + programPath + "'!");
        }
    }

    private void processDirectory(final Path path, final boolean wholeProgram) {
        try (var files = Files.walk(path)) {
            files
                    .filter(f -> f.toFile().isFile())
                    .parallel()
                    .forEach(f -> processFile(path, f, wholeProgram));
        } catch (IOException e) {
            log.warning("Failed to walk over all files in directory '" + path + "'!");
            e.printStackTrace();
        }
    }

    private Optional<Program> readProgram(final Path programPath) {
        try {
            return Optional.of(PARSER.parseFile(programPath.toFile()));
        } catch (Exception e) {
            log.warning("Could not parse file '" + programPath + "' as Scratch project!");
            e.printStackTrace();
            return Optional.empty();
        }
    }

    private void writeResultToOutput(final Path inputFile, final Stream<R> result) throws IOException {
        if (outputPath.isConsoleOutput()) {
            writeResultToConsole(inputFile, result);
        } else {
            writeResultToFile(inputFile, result);
        }
    }

    private void writeResultToConsole(final Path inputFile, final Stream<R> result) {
        // intentionally not in try-with-resources, as we do not want to close System.out
        final PrintWriter pw = new PrintWriter(System.out, true);
        writeResult(inputFile, pw, result);
    }

    private void writeResultToFile(final Path inputFile, final Stream<R> result) throws IOException {
        final Path outputFile = getOutputFilePath(inputFile);

        Files.createDirectories(outputFile.getParent());

        try (
                BufferedWriter bw = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8);
                PrintWriter pw = new PrintWriter(bw)
        ) {
            writeResult(inputFile, pw, result);
        }

        log.info("Wrote processing result of " + inputFile + " to file " + outputFile);
    }

    private Path getOutputFilePath(final Path inputFile) {
        Path outputFile = outputPath.getPath();
        if (inputFile.getParent() != null) {
            outputFile = outputFile.resolve(inputFile.getParent());
        }

        final Path outName = outputFileName(inputFile.getFileName());
        outputFile = outputFile.resolve(outName);
        return outputFile;
    }

    private void writeResult(final Path inputFile, final PrintWriter printWriter, final Stream<R> result) {
        final Iterator<R> lines = result.iterator();
        if (!lines.hasNext()) {
            log.warning("Processing " + inputFile + " resulted in no output!");
            return;
        }

        while (lines.hasNext()) {
            final String output = programPreprocessor.resultToString(lines.next());
            printWriter.println(output);
        }
    }
}
