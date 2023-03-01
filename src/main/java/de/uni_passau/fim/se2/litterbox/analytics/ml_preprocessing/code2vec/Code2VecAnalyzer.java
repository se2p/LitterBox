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
import java.nio.file.Path;
import java.util.logging.Logger;
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

        GeneratePathTask generatePathTask = new GeneratePathTask(program, maxPathLength, includeStage, wholeProgram, isPerScript);
        return generatePathTask.createContextForCode2Vec();
    }

    @Override
    protected Path outputFileName(File inputFile) {
        return Path.of(FilenameUtils.removeExtension(inputFile.getName()));
    }
}
