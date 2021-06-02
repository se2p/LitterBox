package de.uni_passau.fim.se2.litterbox.analytics.refactorings;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractRefactoringFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.KeyPressed;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.MergeEventHandler;

import java.util.ArrayList;

public class MergeEventHandlerFinder extends AbstractRefactoringFinder {

    private ArrayList<Script> eventList = new ArrayList<>();


    @Override
    public void visit(Program program) {
        visitChildren(program);

        if(eventList.size() > 1) {
            refactorings.add(new MergeEventHandler(eventList));
        }
    }


    @Override
    public void visit(Script script) {
        if (script.getEvent() instanceof KeyPressed && script.getStmtList().getNumberOfStatements() > 0) {
            eventList.add(script);
        }
    }

    // TODO add all other events to be able refactored

    @Override
    public String getName() {
        return MergeEventHandler.NAME;
    }
}
