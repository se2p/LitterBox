package de.uni_passau.fim.se2.litterbox.analytics.refactorings;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractRefactoringFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.KeyPressed;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.MergeEventHandler;

import java.util.ArrayList;

public class MergeEventHandlerFinder extends AbstractRefactoringFinder {

    ArrayList<Script> eventList = new ArrayList<>();


    @Override
    public void visit(Program program) {
        visitChildren(program);

        if(eventList.size() > 0)
            refactorings.add(new MergeEventHandler(eventList));
    }


    @Override
    public void visit(KeyPressed keyPressed) {
        eventList.add((Script) keyPressed.getParentNode());
    }

    // TODO add all other events to be able refactored

    @Override
    public String getName() {
        return MergeEventHandler.NAME;
    }
}
