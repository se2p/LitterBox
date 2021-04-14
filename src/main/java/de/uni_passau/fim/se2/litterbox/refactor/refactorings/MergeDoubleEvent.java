package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Event;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;

public class MergeDoubleEvent implements Refactoring {

    private final Event event1;
    private final Event event2;
    private static final String NAME = "merge_double_event";

    public MergeDoubleEvent(Event event1, Event event2) {
        this.event1 = event1;
        this.event2 = event2;
    }

    @Override
    public Program apply(Program program) {
        CloneVisitor cloneVisitor = new CloneVisitor();
        Program refactored = cloneVisitor.apply(program);
        StmtList stmts;
        if (event2.hasChildren()) {
            stmts = (StmtList) event2.getChildren();
            ((Script) event1.getParentNode()).getStmtList().getStmts().addAll(stmts.getStmts());
        }
        event2.getParentNode().getChildren().remove(event2);
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
