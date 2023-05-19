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

import de.uni_passau.fim.se2.litterbox.ast.model.Program;

import java.util.List;

public class GeneratePathTask {

    private final Program program;
    private final int maxPathLength;
    private final boolean includeStage;
    private final boolean wholeProgram;
    private final boolean includeDefaultSprites;

    public GeneratePathTask(Program program, int maxPathLength, boolean includeStage, boolean wholeProgram,
                            boolean includeDefaultSprites) {
        this.program = program;
        this.maxPathLength = maxPathLength;
        this.includeStage = includeStage;
        this.wholeProgram = wholeProgram;
        this.includeDefaultSprites = includeDefaultSprites;
    }

    public List<ProgramFeatures> createContextForCode2Vec() {
        PathGenerator pathGenerator = new PathGenerator(program, maxPathLength, includeStage, wholeProgram,
                includeDefaultSprites);
        // pathGenerator.printLeafsPerSprite();
        return pathGenerator.generatePaths();
    }
}
