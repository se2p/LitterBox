/*
 * Copyright (C) 2019-2024 LitterBox contributors
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

import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.MultiBlockIssue;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptEntity;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @QuestionType Multiple Choice
 * @NumAnswers 1
 * @NumChoices 1
 * @Highlighted Two scripts
 * @Context Whole program
 */
public class ScriptExecutionOrderDifferentActors extends AbstractQuestionFinder {

    Map<String, Map<String, List<Script>>> scriptsWithEvent;

    @Override
    public void visit(Program node) {
        scriptsWithEvent = new HashMap<>();
        super.visit(node);

        for (Map.Entry<String, Map<String, List<Script>>> entry : scriptsWithEvent.entrySet()) {
            String event = entry.getKey();
            Map<String, List<Script>> actorsWithEvent = entry.getValue();
            if (actorsWithEvent.size() > 1) {
                List<ScriptEntity> scriptEntities = new ArrayList<>();
                List<ASTNode> nodes = new ArrayList<>();
                Hint hint = Hint.fromKey(getName());
                String actor1;
                String actor2;
                Script script1;
                Script script2;

                List<String> actors = new ArrayList<>(actorsWithEvent.keySet());
                String stage = "Stage";
                if (actorsWithEvent.containsKey(stage)) {
                    actor1 = stage;
                    actors.remove(stage);
                    actor2 = actors.getFirst();
                    script1 = actorsWithEvent.get(actor1).getFirst();
                    script2 = actorsWithEvent.get(actor2).getFirst();
                    hint.setParameter(Hint.CONDITION, "");
                } else {
                    actor1 = actors.get(0);
                    actor2 = actors.get(1);
                    script1 = actorsWithEvent.get(actor1).getFirst();
                    script2 = actorsWithEvent.get(actor2).getFirst();
                    hint.setParameter(Hint.CONDITION, " Suppose " + actor2 + " is in front of " + actor1 + ".");
                }

                hint.setParameter(Hint.HINT_VARIABLE1, actor1);
                hint.setParameter(Hint.HINT_VARIABLE2, actor2);
                hint.setParameter(Hint.EVENT, event);
                hint.setParameter(Hint.CHOICES, actor1);
                hint.setParameter(Hint.ANSWER, actor2);

                scriptEntities.add(script1);
                scriptEntities.add(script2);
                nodes.add(script1.getEvent());
                nodes.add(script2.getEvent());

                MultiBlockIssue issue = new MultiBlockIssue(
                        this,
                        IssueSeverity.LOW,
                        program,
                        currentActor,
                        scriptEntities,
                        nodes,
                        node.getMetadata(),
                        hint);

                addIssue(issue);
            }
        }
    }

    @Override
    public void visit(Script node) {
        if (!(node.getEvent() instanceof Never)) {
            scriptsWithEvent
                    .computeIfAbsent(ScratchBlocksVisitor.of(node.getEvent()), k -> new HashMap<>())
                    .computeIfAbsent(ScratchBlocksVisitor.of(currentActor.getIdent()), k -> new ArrayList<>())
                    .add(node);
        }
    }

    @Override
    public String getName() {
        return "script_execution_order_different_actors";
    }
}
