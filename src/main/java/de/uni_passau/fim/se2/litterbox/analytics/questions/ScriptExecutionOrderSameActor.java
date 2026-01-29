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
package de.uni_passau.fim.se2.litterbox.analytics.questions;

import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @QuestionType Yes or No
 * @Highlighted Two scripts
 * @Context Single actor
 */
public class ScriptExecutionOrderSameActor extends AbstractQuestionFinder {

    Map<String, List<Script>> scriptsWithEvent;

    @Override
    public void visit(ActorDefinition node) {
        currentActor = node;
        scriptsWithEvent = new HashMap<>();
        visit(node.getScripts());
        for (Map.Entry<String, List<Script>> entry : scriptsWithEvent.entrySet()) {
            String event = entry.getKey();
            List<Script> scripts = entry.getValue();

            if (scripts.size() > 1) {
                Script script1 = scripts.get(0);
                Script script2 = scripts.get(1);

                // Use script1's event as the primary node
                QuestionBuilder builder = prepareQuestionBuilder(script1.getEvent())
                        .withScript(script1)
                        .addAdditionalScript(script2)
                        .withCorrectAnswers("no")
                        .withQuestionText(QuestionText.fromKey(getName()))
                        .withQuestionParameter("EVENT", event);

                addQuestion(builder);
                break;
            }
        }
    }

    @Override
    public void visit(Script node) {
        if (!(node.getEvent() instanceof Never)) {
            scriptsWithEvent.computeIfAbsent(ScratchBlocksVisitor.of(node.getEvent()),
                    k -> new ArrayList<>()).add(node);
        }
    }

    @Override
    public String getName() {
        return "script_execution_order_same_actor";
    }

    @Override
    public ProgramDimension getProgramDimension() {
        return ProgramDimension.EXECUTION;
    }

    @Override
    public ScopeOfFocus getScopeOfFocus() {
        return ScopeOfFocus.RELATION;
    }

    @Override
    public QuestionType getQuestionType() {
        return QuestionType.YES_NO;
    }
}
