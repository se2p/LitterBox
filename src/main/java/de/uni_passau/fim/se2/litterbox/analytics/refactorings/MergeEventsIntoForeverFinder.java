package de.uni_passau.fim.se2.litterbox.analytics.refactorings;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractRefactoringFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptList;
import de.uni_passau.fim.se2.litterbox.ast.model.event.KeyPressed;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.MergeEventsIntoForever;

import java.util.ArrayList;
import java.util.List;

public class MergeEventsIntoForeverFinder extends AbstractRefactoringFinder {

    @Override
    public void visit(ScriptList scriptList) {

        List<Script> eventList = new ArrayList<>();
        for (Script script : scriptList.getScriptList()) {
            if (script.getEvent() instanceof KeyPressed && script.getStmtList().getNumberOfStatements() > 0) {
                eventList.add(script);
            }
        }

        if(eventList.size() > 0) {
            refactorings.add(new MergeEventsIntoForever(eventList));
        }
    }

    @Override
    public String getName() {
        return MergeEventsIntoForever.NAME;
    }
}
