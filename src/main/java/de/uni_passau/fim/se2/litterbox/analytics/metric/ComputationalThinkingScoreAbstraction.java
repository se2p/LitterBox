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
package de.uni_passau.fim.se2.litterbox.analytics.metric;

import de.uni_passau.fim.se2.litterbox.analytics.MetricExtractor;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.event.StartedAsClone;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public class ComputationalThinkingScoreAbstraction implements MetricExtractor<Program>, ScratchVisitor {
    public static final String NAME = "ct_score_abstraction";

    private int score = 0;

    // Score 1: More than one sprite with script
    private int numSpritesWithScripts = 0;

    @Override
    public double calculateMetric(Program program) {
        score = 0;
        numSpritesWithScripts = 0;
        program.accept(this);
        return score;
    }

    @Override
    public void visit(StartedAsClone node) {
        score = 3;
    }

    @Override
    public void visit(ProcedureDefinition node) {
        score = Math.max(2, score);
    }

    @Override
    public void visit(ActorDefinition node) {
        int numScripts = node.getScripts().getScriptList().size();
        if (numScripts > 1) {
            numSpritesWithScripts++;
            if (numSpritesWithScripts > 1) {
                score = Math.max(1, score);
            }
        }
        visit(node.getScripts());
        visit(node.getProcedureDefinitionList());
    }

    @Override
    public String getName() {
        return NAME;
    }
}
