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
package de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.code2;

import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.MLPreprocessorCommonOptions;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.code2.pathgeneration.*;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.code2.pathgeneration.program_relation.ProgramRelationFactory;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;

import java.io.File;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class Code2SeqAnalyzer extends Code2Analyzer {

    private static final Logger log = Logger.getLogger(Code2SeqAnalyzer.class.getName());

    public Code2SeqAnalyzer(
            final MLPreprocessorCommonOptions commonOptions, final int maxPathLength, final boolean isPerScript
    ) {
        super(commonOptions, maxPathLength, isPerScript);
    }

    @Override
    public Stream<ProgramFeatures> process(File inputFile) {
        final Program program = extractProgram(inputFile);
        if (program == null) {
            log.warning("Program was null. File name was '" + inputFile.getName() + "'");
            return Stream.empty();
        }

        final ProgramRelationFactory programRelationFactory = new ProgramRelationFactory();
        final PathFormatOptions pathFormatOptions = new PathFormatOptions("|", "|", "|", "", "", true, true);
        PathGenerator pathGenerator = PathGeneratorFactory.createPathGenerator(
                pathType, maxPathLength, includeStage, program, includeDefaultSprites, pathFormatOptions,
                programRelationFactory, actorNameNormalizer
        );
        final GeneratePathTask generatePathTask = new GeneratePathTask(pathGenerator);

        return generatePathTask.createContext().stream();
    }
}
