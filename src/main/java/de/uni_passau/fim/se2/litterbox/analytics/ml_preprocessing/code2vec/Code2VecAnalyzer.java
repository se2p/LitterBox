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

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Code2VecAnalyzer extends MLPreprocessingAnalyzer<ProgramFeatures> {

    private static final Logger log = Logger.getLogger(Code2VecAnalyzer.class.getName());

    private final PathType pathType;
    private final int maxPathLength;

    public Code2VecAnalyzer(final MLPreprocessorCommonOptions commonOptions, int maxPathLength, boolean isPerScript) {
        super(commonOptions);

        this.maxPathLength = maxPathLength;

        if (isPerScript) {
            this.pathType = PathType.SCRIPT;
        } else if (wholeProgram) {
            this.pathType = PathType.PROGRAM;
        } else {
            this.pathType = PathType.SPRITE;
        }
    }

    @Override
    public Stream<ProgramFeatures> process(File inputFile) {
        final Program program = extractProgram(inputFile);
        if (program == null) {
            log.warning("Program was null. File name was '" + inputFile.getName() + "'");
            return Stream.empty();
        }
        PathGenerator pathGenerator = PathGeneratorFactory.createPathGenerator(
                pathType, maxPathLength, includeStage, program, includeDefaultSprites
        );
        GeneratePathTask generatePathTask = new GeneratePathTask(pathGenerator);
        return generatePathTask.createContextForCode2Vec().stream();
    }

    @Override
    protected String resultToString(ProgramFeatures result) {
        return result.toString();
    }

    @Override
    protected Path outputFileName(File inputFile) {
        return Path.of(FilenameUtils.removeExtension(inputFile.getName()));
    }

    @Override
    protected void check(File fileEntry, Path csv) throws IOException {
        if (this.pathType == PathType.SCRIPT) {
            runProcessingSteps(fileEntry);
        } else {
            super.check(fileEntry, csv);
        }
    }

    private void runProcessingSteps(File inputFile) {
        final var output = process(inputFile);
        var outputList = output.toList();
        this.writeResultPerScriptToOutput(inputFile, outputList);
    }

    private void writeResultPerScriptToOutput(File inputFile, List<ProgramFeatures> result) {
        if (result.isEmpty()) {
            return;
        }
        if (outputPath.isConsoleOutput()) {
            System.out.println(result);
        } else {
            writeResultPerScriptToFile(inputFile, result);
        }
    }

    private void writeResultPerScriptToFile(File inputFile, List<ProgramFeatures> result) {
        for (ProgramFeatures token : result) {
            Path outName = outputFileName(inputFile);
            Path outputFile = outputPath.getPath().resolve(outName + "_" + token.getName());
            if (Files.exists(outputFile)) {
                log.warning("A duplicated script has been skipped " + outputFile);
                continue;
            }
            writeProgramFeaturesToFile(outputFile, token);
        }
    }

    private static void writeProgramFeaturesToFile(Path outputFile, ProgramFeatures token) {
        try (BufferedWriter bw = Files.newBufferedWriter(outputFile)) {
            bw.write(token.getFeatures().stream().map(ProgramRelation::toString).collect(Collectors.joining(" ")));
            bw.flush();
        } catch (IOException e) {
            log.severe("Exception in writing the file " + outputFile + "Error message " + e.getMessage());
        }
    }
}
