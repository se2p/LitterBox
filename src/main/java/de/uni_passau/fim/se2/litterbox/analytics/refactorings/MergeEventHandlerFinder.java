package de.uni_passau.fim.se2.litterbox.analytics.refactorings;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractRefactoringFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Event;
import de.uni_passau.fim.se2.litterbox.ast.model.event.KeyPressed;
import de.uni_passau.fim.se2.litterbox.ast.model.event.SpriteClicked;
import de.uni_passau.fim.se2.litterbox.ast.model.event.StageClicked;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.MergeEventHandler;

import java.util.ArrayList;

public class MergeEventHandlerFinder extends AbstractRefactoringFinder {

    ArrayList<Event> eventList = new ArrayList<>();


    @Override
    public void visit(Program program) {
        visitChildren(program);

        if(eventList.size() > 0)
            refactorings.add(new MergeEventHandler(eventList));
    }


    @Override
    public void visit(KeyPressed keyPressed) {
        eventList.add(keyPressed);
    }

    @Override
    public void visit(SpriteClicked spriteClicked) {
        eventList.add(spriteClicked);
    }

    @Override
    public void visit(StageClicked stageClicked) {
        eventList.add(stageClicked);
    }

    // TODO add all other events to be able refactored

    @Override
    public String getName() {
        return MergeEventHandler.NAME;
    }
}
