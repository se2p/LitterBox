package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;

public class MergeDoubleIf implements Refactoring {

    private final IfThenStmt if1;
    private final IfThenStmt if2;
    private static final String NAME = "merge_double_if";

    public MergeDoubleIf(IfThenStmt if1, IfThenStmt if2) {
        this.if1 = if1;
        this.if2 = if2;
    }

    @Override
    public Program apply(Program program) {
        Program refactored = program.deepCopy(); // even needed?
        if1.getThenStmts().getStmts().addAll(if2.getThenStmts().getStmts());

        StmtList stmtList = (StmtList) if2.getParentNode();
        stmtList.getStmts().remove(if2);
        return refactored;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String toString() {
        return NAME + "(" + if1.getUniqueName() + ", " + if2.getUniqueName() + ")";
    }
}
