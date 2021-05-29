package de.uni_passau.fim.se2.litterbox.analytics.refactorings;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractRefactoringFinder;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.ExtractEventHandler;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.MergeEventHandler;

public class ExtractEventHandlerFinder extends AbstractRefactoringFinder {
    @Override
    public String getName() {
        return ExtractEventHandler.NAME;
    }
}
