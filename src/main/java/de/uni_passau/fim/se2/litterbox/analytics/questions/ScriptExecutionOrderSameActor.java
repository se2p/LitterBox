package de.uni_passau.fim.se2.litterbox.analytics.questions;

import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.MultiBlockIssue;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @QuestionType Yes or No
 * @Highlighted Two scripts
 * @Context Actor
 */
public class ScriptExecutionOrderSameActor extends AbstractQuestionFinder {

    Map<String, List<Script>> scriptsWithEvent;

    @Override
    public void visit(ActorDefinition node) {
        scriptsWithEvent = new HashMap<>();
        visit(node.getScripts());
        for (Map.Entry<String, List<Script>> entry : scriptsWithEvent.entrySet()) {
            String event = entry.getKey();
            List<Script> scripts = entry.getValue();

            if (scripts.size() > 1) {
                List<ScriptEntity> scriptEntities = new ArrayList<>();
                List<ASTNode> nodes = new ArrayList<>();

                scriptEntities.add(node.getScripts().getScript(0));
                scriptEntities.add(node.getScripts().getScript(1));
                nodes.add(node.getScripts().getScript(0));
                nodes.add(node.getScripts().getScript(1));

                Hint hint = new Hint(getName());
                hint.setParameter(Hint.EVENT, event);
                hint.setParameter(Hint.ANSWER, NO);

                MultiBlockIssue issue = new MultiBlockIssue(
                        new ScriptExecutionOrderSameActor(),
                        IssueSeverity.LOW,
                        program,
                        node,
                        scriptEntities,
                        nodes,
                        node.getMetadata(),
                        hint);

                addIssue(issue);
                break;
            }
        }
    }

    @Override
    public void visit(Script node) {
        scriptsWithEvent.computeIfAbsent(node.getEvent().getScratchBlocks(),
                k -> new ArrayList<>()).add(node);
    }

    @Override
    public String getName() {
        return "script_execution_order_same_actor";
    }
}
