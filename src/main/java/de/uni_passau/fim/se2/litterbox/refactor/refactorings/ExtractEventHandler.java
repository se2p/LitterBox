package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;

public class ExtractEventHandler implements Refactoring{

    public static final String NAME = "extract_event_handler";

    private RepeatForeverStmt loop;
    private IfThenStmt ifThenStmt;

    public ExtractEventHandler(RepeatForeverStmt loop, IfThenStmt ifThenStmt) {
        this.loop = loop;
        this.ifThenStmt = ifThenStmt;

        // Check Precoditions
    }

    @Override
    public Program apply(Program program) {
        return null;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
