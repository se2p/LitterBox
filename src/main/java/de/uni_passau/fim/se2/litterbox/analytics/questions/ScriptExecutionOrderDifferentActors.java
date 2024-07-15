package de.uni_passau.fim.se2.litterbox.analytics.questions;

import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.MultiBlockIssue;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptEntity;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @QuestionType Multiple Choice
 * @NumAnswers 1
 * @NumChoices 2
 * @Highlighted Two scripts
 * @Context Program
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
                Hint hint = new Hint(getName());
                String actor1;
                String actor2;
                Script script1;
                Script script2;

                List<String> actors = new ArrayList<>(actorsWithEvent.keySet());
                String stage = "Stage";
                if (actorsWithEvent.containsKey(stage)) {
                    actor1 = stage;
                    actors.remove(stage);
                    actor2 = actors.get(0);
                    script1 = actorsWithEvent.get(actor1).get(0);
                    script2 = actorsWithEvent.get(actor2).get(0);
                    hint.setParameter(Hint.CONDITION, "");
                }
                else {
                    actor1 = actors.get(0);
                    actor2 = actors.get(1);
                    script1 = actorsWithEvent.get(actor1).get(0);
                    script2 = actorsWithEvent.get(actor2).get(0);
                    hint.setParameter(Hint.CONDITION, " Suppose " + actor2 + " is in front of " + actor1 + ".");
                }

                hint.setParameter(Hint.HINT_VARIABLE1, actor1);
                hint.setParameter(Hint.HINT_VARIABLE2, actor2);
                hint.setParameter(Hint.EVENT, event);
                hint.setParameter(Hint.CHOICES, actor2);
                hint.setParameter(Hint.ANSWER, actor1);

                scriptEntities.add(script1);
                scriptEntities.add(script2);
                nodes.add(!(script1.getEvent() instanceof Never) ? script1.getEvent() : script1.getStmtList().getStmts().get(0));
                nodes.add(!(script2.getEvent() instanceof Never) ? script2.getEvent() : script2.getStmtList().getStmts().get(0));

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
        scriptsWithEvent.computeIfAbsent(node.getEvent().getScratchBlocksWithoutNewline(),
                k -> new HashMap<>()).computeIfAbsent(currentActor.getIdent().getScratchBlocks(),
                k -> new ArrayList<>()).add(node);
    }

    @Override
    public String getName() {
        return "script_execution_order_different_actors";
    }
}
