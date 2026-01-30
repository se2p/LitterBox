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

import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Generates a question asking about the execution order of scripts in different actors.
 *
 */
public class ScriptExecutionOrderDifferentActors extends AbstractQuestionFinder {

    public static final String FINDER_NAME = "script_execution_order_different_actors";
    private static final String STAGE_NAME = "Stage";
    private static final String ACTOR1_PARAM = "ACTOR1";
    private static final String ACTOR2_PARAM = "ACTOR2";

    Map<String, Map<String, List<Script>>> scriptsWithEvent;

    @Override
    public void visit(Program node) {
        scriptsWithEvent = new LinkedHashMap<>();
        super.visit(node);

        for (Map.Entry<String, Map<String, List<Script>>> entry : scriptsWithEvent.entrySet()) {
            String event = entry.getKey();
            Map<String, List<Script>> actorsWithEvent = entry.getValue();
            if (actorsWithEvent.size() > 1) {
                String actor1;
                String actor2;
                Script script1;
                Script script2;
                String condition;

                List<String> actors = new ArrayList<>(actorsWithEvent.keySet());
                if (actorsWithEvent.containsKey(STAGE_NAME)) {
                    actor1 = STAGE_NAME;
                    actors.remove(STAGE_NAME);
                    actor2 = actors.getFirst();
                    script1 = actorsWithEvent.get(actor1).getFirst();
                    script2 = actorsWithEvent.get(actor2).getFirst();
                    condition = "";
                } else {
                    actor1 = actors.get(0);
                    actor2 = actors.get(1);
                    script1 = actorsWithEvent.get(actor1).getFirst();
                    script2 = actorsWithEvent.get(actor2).getFirst();
                    condition = " Suppose " + actor2 + " is in front of " + actor1 + ".";
                }

                // Use script1's event as the primary node
                QuestionBuilder builder = prepareQuestionBuilder(script1.getEvent())
                        .withScript(script1)
                        .addAdditionalScript(script2)
                        .withChoices(List.of(actor1))
                        .withCorrectAnswers(actor2)
                        .withQuestionParameter(ACTOR1_PARAM, actor1)
                        .withQuestionParameter(ACTOR2_PARAM, actor2)
                        .withQuestionParameter(QuestionText.EVENT, event)
                        .withQuestionParameter(QuestionText.CONDITION, condition);

                addQuestion(builder);
            }
        }
    }

    @Override
    public void visit(Script node) {
        if (!(node.getEvent() instanceof Never)) {
            scriptsWithEvent
                    .computeIfAbsent(ScratchBlocksVisitor.of(node.getEvent()), k -> new LinkedHashMap<>())
                    .computeIfAbsent(ScratchBlocksVisitor.of(currentActor.getIdent()), k -> new ArrayList<>())
                    .add(node);
        }
    }

    @Override
    public String getName() {
        return FINDER_NAME;
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
        return QuestionType.MULTIPLE_CHOICE;
    }
}
