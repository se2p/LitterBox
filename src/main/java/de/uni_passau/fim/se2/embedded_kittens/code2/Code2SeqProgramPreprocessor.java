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
package de.uni_passau.fim.se2.embedded_kittens.code2;

import java.util.stream.Stream;

import de.uni_passau.fim.se2.embedded_kittens.MLPreprocessorCommonOptions;
import de.uni_passau.fim.se2.embedded_kittens.code2.pathgeneration.GeneratePathTask;
import de.uni_passau.fim.se2.embedded_kittens.code2.pathgeneration.PathFormatOptions;
import de.uni_passau.fim.se2.embedded_kittens.code2.pathgeneration.PathGenerator;
import de.uni_passau.fim.se2.embedded_kittens.code2.pathgeneration.PathGeneratorFactory;
import de.uni_passau.fim.se2.embedded_kittens.code2.pathgeneration.PathType;
import de.uni_passau.fim.se2.embedded_kittens.code2.pathgeneration.ProgramFeatures;
import de.uni_passau.fim.se2.embedded_kittens.code2.pathgeneration.program_relation.ProgramRelationFactory;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;

public class Code2SeqProgramPreprocessor extends Code2ProgramPreprocessor {

    public Code2SeqProgramPreprocessor(
        final MLPreprocessorCommonOptions commonOptions, final int maxPathLength, final boolean isPerScript
    ) {
        super(commonOptions, maxPathLength, isPerScript);
    }

    @Override
    public Stream<ProgramFeatures> processSprites(Program program) {
        return process(program, super.pathType);
    }

    @Override
    public Stream<ProgramFeatures> processWholeProgram(Program program) {
        return process(program, PathType.PROGRAM);
    }

    private Stream<ProgramFeatures> process(final Program program, final PathType pathType) {
        final ProgramRelationFactory programRelationFactory = new ProgramRelationFactory();
        final PathFormatOptions pathFormatOptions = new PathFormatOptions("|", "|", "|", "", "", true, true);
        PathGenerator pathGenerator = PathGeneratorFactory.createPathGenerator(
            pathType, maxPathLength, commonOptions.includeStage(), program, commonOptions.includeDefaultSprites(),
            pathFormatOptions, programRelationFactory, commonOptions.actorNameNormalizer()
        );
        final GeneratePathTask generatePathTask = new GeneratePathTask(pathGenerator);

        return generatePathTask.createContext().stream();
    }

    @Override
    public String resultToString(ProgramFeatures result) {
        return result.toString();
    }
}
