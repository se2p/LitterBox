package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;

public class ExtractEventHandler implements Refactoring{

    public static final String NAME = "extract_event_handler";

    @Override
    public Program apply(Program program) {
        return null;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
