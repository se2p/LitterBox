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
package de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.ggnn;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;

import java.nio.file.Path;

public class GenerateGraphTask {
    private final Path inputPath;
    private final Program program;
    private final boolean isStageIncluded;
    private final boolean isWholeProgram;
    private final boolean isDotStringGraph;
    private final String labelName;

    public GenerateGraphTask(Program program, Path inputPath, boolean isStageIncluded, boolean isWholeProgram,
                             boolean isDotStringGraph, String labelName) {
        this.inputPath = inputPath;
        this.program = program;
        this.isStageIncluded = isStageIncluded;
        this.isWholeProgram = isWholeProgram;
        this.isDotStringGraph = isDotStringGraph;
        this.labelName = labelName;
    }

    public String generateGraphData() {
        GraphGenerator graphGenerator = new GraphGenerator(program, isStageIncluded, isWholeProgram, isDotStringGraph,
                                                           labelName);
        if (!isWholeProgram) {
            graphGenerator.extractGraphsPerSprite();
        }

        return graphGenerator.generateGraphs(inputPath.toString());
    }
}
