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
package de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.code2vec;

import de.uni_passau.fim.se2.litterbox.analytics.MLPreprocessingAnalyzer;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.MLPreprocessorCommonOptions;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Code2VecAnalyzer extends MLPreprocessingAnalyzer {
    private static final Logger log = Logger.getLogger(Code2VecAnalyzer.class.getName());
    private final int maxPathLength;
    private final boolean isPerScript;

    public Code2VecAnalyzer(final MLPreprocessorCommonOptions commonOptions, int maxPathLength, boolean isPerScript) {
        super(commonOptions);
        this.maxPathLength = maxPathLength;
        this.isPerScript = isPerScript;
    }

    @Override
    public Stream<String> process(File inputFile) {
        final Program program = extractProgram(inputFile);
        if (program == null) {
            log.warning("Program was null. File name was '" + inputFile.getName() + "'");
            return Stream.empty();
        }
        // TODO use builder instead?
        PathGenerator pathGenerator = PathGeneratorFactory.createPathGenerator(wholeProgram, isPerScript, maxPathLength, includeStage, program);
        GeneratePathTask generatePathTask = new GeneratePathTask(pathGenerator);
        List<ProgramFeatures> features = generatePathTask.createContextForCode2Vec();
        return generatePathTask.featuresToString(features, true);
    }

    @Override
    protected Path outputFileName(File inputFile) {
        return Path.of(FilenameUtils.removeExtension(inputFile.getName()));
    }

    @Override
    protected void check(File fileEntry, String csv) throws IOException {
        if (this.isPerScript) {
            runProcessingSteps(fileEntry);
        } else super.check(fileEntry, csv);
    }

    private void runProcessingSteps(File inputFile) throws IOException {
        final Stream<String> output = process(inputFile);
        List<String> outputList = output.collect(Collectors.toList());
        this.writeResultPerScriptsToOutput(inputFile, outputList);
    }

    private void writeResultPerScriptsToOutput(File inputFile, List<String> result) throws IOException {
        if (result.isEmpty()) {
            log.warning("The processing step returned no output For input File " + inputFile.getName());
            return;
        }
        if (outputPath.isConsoleOutput()) {
            System.out.println(result);
        } else {
            writeResultPerScriptToFile(inputFile, result);
        }
    }

    private void writeResultPerScriptToFile(File inputFile, List<String> result) throws IOException {
        for (String token : result) {
            Path outName = outputFileName(inputFile);
            Path outputFile = outputPath.getPath().resolve(outName + getScriptName(token));
            if (Files.exists(outputFile))
                log.severe("Overriding script result " + outputFile);
            try (BufferedWriter bw = Files.newBufferedWriter(outputFile)) {
                bw.write(removeScriptNameFromToken(token));
                bw.flush();
            }
            //log.info("Wrote processing result of " + inputFile + " to file " + outputFile);
        }
    }

    // TODO the next 2 methods is a dirty way around, better to change the return type of 'process' method

    private String getScriptName(String token) {
        return token.split(" ")[0];
    }

    private String removeScriptNameFromToken(String token) {
        int index = token.indexOf(" ");
        return token.substring(index + 1);
    }
}
