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
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class GeneratePathTask {

    private final Program program;
    private final int maxPathLength;
    private final boolean includeStage;
    private final boolean wholeProgram;

    public GeneratePathTask(Program program, int maxPathLength, boolean includeStage, boolean wholeProgram) {
        this.program = program;
        this.maxPathLength = maxPathLength;
        this.includeStage = includeStage;
        this.wholeProgram = wholeProgram;
    }

    public String createContextForCode2Vec() {
        PathGenerator pathGenerator = new PathGenerator(program, maxPathLength, includeStage, wholeProgram);
        // pathGenerator.printLeafsPerSprite();
        List<ProgramFeatures> programs = pathGenerator.generatePaths();
        return featuresToString(programs);
    }

    private String featuresToString(List<ProgramFeatures> features) {
        if (features == null || features.isEmpty()) {
            return "";
        }

        List<String> spriteOutputs = new ArrayList<>();

        for (ProgramFeatures singleSpriteFeatures : features) {
            StringBuilder builder = new StringBuilder();

            String toPrint;
            toPrint = singleSpriteFeatures.toString();
            builder.append(toPrint);

            spriteOutputs.add(builder.toString());
        }

        return StringUtils.join(spriteOutputs, "\n");
    }
}
