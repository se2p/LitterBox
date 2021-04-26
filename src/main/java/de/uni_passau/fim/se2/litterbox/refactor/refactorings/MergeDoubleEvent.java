package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Event;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

public class MergeDoubleEvent implements Refactoring {

    private final Event event1;
    private final Event event2;
    private final Script script1;
    private final Script script2;
    private final ScriptList scriptList;
    private static final String NAME = "merge_double_event";

    public MergeDoubleEvent(Event event1, Event event2) {
        this.event1 = Preconditions.checkNotNull(event1);
        this.event2 = Preconditions.checkNotNull(event2);
        this.script1 = (Script) event1.getParentNode();
        this.script2 = (Script) event2.getParentNode();
        this.scriptList = (ScriptList) script2.getParentNode();
    }

    @Override
    public Program apply(Program program) {
        Program refactored = program.deepCopy();
        StmtList stmts;
        if (script2.getStmtList().hasStatements()) {
            stmts = script2.getStmtList();
            script1.getStmtList().getStmts().addAll(stmts.getStmts());
        }
        scriptList.getScriptList().remove(script2);
        return refactored;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String toString() {
        return NAME + "(" + event1.getUniqueName() + ", " + event2.getUniqueName() + ")";
    }
}
