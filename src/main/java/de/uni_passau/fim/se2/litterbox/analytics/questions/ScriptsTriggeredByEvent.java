package de.uni_passau.fim.se2.litterbox.analytics.questions;

import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.IssueBuilder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Event;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @QuestionType Number
 * @Highlighted Nothing
 * @Context Whole program
 */
public class ScriptsTriggeredByEvent extends AbstractQuestionFinder {

    private Map<Event, List<Script>> triggeredScripts;

    @Override
    public void visit(Program node) {
        triggeredScripts = new HashMap<>();
        super.visit(node);
        currentScript = null;
        currentProcedure = null;

        triggeredScripts.forEach((event, scripts) -> {
            if (!(event instanceof Never)) {
                IssueBuilder builder = prepareIssueBuilder().withSeverity(IssueSeverity.LOW);
                Hint hint = Hint.fromKey(getName());
                hint.setParameter(Hint.EVENT, ScratchBlocksVisitor.of(event));
                hint.setParameter(Hint.ANSWER, Integer.toString(scripts.size()));
                addIssue(builder.withHint(hint));
            }
        });
    }

    @Override
    public void visit(Script node) {
        Event event = node.getEvent();
        List<Script> list = triggeredScripts.getOrDefault(event, new ArrayList<>());
        list.add(node);
        triggeredScripts.put(event, list);
    }

    @Override
    public String getName() {
        return "scripts_triggered_by_event";
    }
}
