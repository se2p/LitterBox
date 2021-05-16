package de.uni_passau.fim.se2.litterbox.analytics.refactorings;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractRefactoringFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptList;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Event;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.MergeDoubleEvent;

import java.util.List;

public class DoubleEventFinder extends AbstractRefactoringFinder {

    private static final String NAME = "double_event_finder";

    @Override
    public void visit(ScriptList node) {
        final List<Script> scriptList = node.getScriptList();

        for (Script script1 : scriptList) {
            Event event1 = script1.getEvent();
            for (Script script2 : scriptList) {
                Event event2 = script2.getEvent();
                if (!(script1 == script2)) {
                    if (event1.getUniqueName().equals(event2.getUniqueName())) {
                        refactorings.add(new MergeDoubleEvent(event1, event2));
                    }
                }
            }
        }
        visitChildren(node);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
