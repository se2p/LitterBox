/*
 * Copyright (C) 2019-2022 LitterBox contributors
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

import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.MLOutputPath;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.MLPreprocessorCommonOptions;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.shared.ActorNameNormalizer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Stream;

public abstract class MLPreprocessingAnalyzer<R> extends Analyzer {
    private static final Logger log = Logger.getLogger(MLPreprocessingAnalyzer.class.getName());

    protected final MLOutputPath outputPath;
    protected final boolean includeStage;
    protected final boolean wholeProgram;
    protected final boolean includeDefaultSprites;
    protected final ActorNameNormalizer actorNameNormalizer;

    /**
     * Sets up an analyzer that extracts the necessary information for a machine learning model from a program.
     *
     * @param commonOptions Some common options used for all machine learning preprocessors.
     */
    protected MLPreprocessingAnalyzer(final MLPreprocessorCommonOptions commonOptions) {
        super(commonOptions.inputPath(), null, commonOptions.deleteAfterwards());

        this.outputPath = commonOptions.outputPath();
        this.includeStage = commonOptions.includeStage();
        this.wholeProgram = commonOptions.wholeProgram();
        this.includeDefaultSprites = commonOptions.includeDefaultSprites();
        this.actorNameNormalizer = commonOptions.actorNameNormalizer();
    }

    protected abstract Stream<R> process(File inputFile) throws IOException;

    protected abstract String resultToString(R result);

    protected abstract Path outputFileName(File inputFile);

    @Override
    protected void check(final File fileEntry, final Path csv) throws IOException {
        runProcessingSteps(fileEntry);
    }

    private void runProcessingSteps(final File inputFile) throws IOException {
        final Stream<String> output = process(inputFile).filter(Objects::nonNull).map(this::resultToString);
        writeResultToOutput(inputFile, output);
    }

    private void writeResultToOutput(final File inputFile, final Stream<String> result) throws IOException {
        if (outputPath.isConsoleOutput()) {
            writeResultToConsole(inputFile, result);
        } else {
            writeResultToFile(inputFile, result);
        }
    }

    private void writeResultToConsole(final File inputFile, final Stream<String> result) {
        // intentionally not in try-with-resources, as we do not want to close System.out
        final PrintWriter pw = new PrintWriter(System.out, true);
        writeResult(inputFile, pw, result);
    }

    private void writeResultToFile(final File inputFile, final Stream<String> result) throws IOException {
        Files.createDirectories(outputPath.getPath());
        final Path outName = outputFileName(inputFile);
        final Path outputFile = outputPath.getPath().resolve(outName);

        try (
                BufferedWriter bw = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8);
                PrintWriter pw = new PrintWriter(bw);
        ) {
            writeResult(inputFile, pw, result);
        }

        log.info("Wrote processing result of " + inputFile + " to file " + outputFile);
    }

    private void writeResult(final File inputFile, final PrintWriter printWriter, final Stream<String> result) {
        final Iterator<String> lines = result.iterator();
        if (!lines.hasNext()) {
            log.warning("Processing " + inputFile + " resulted in no output!");
            return;
        }

        while (lines.hasNext()) {
            printWriter.println(lines.next());
        }
    }
}
