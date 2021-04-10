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
        Program refactored = program.copy(); // even needed?
        if1.getThenStmts().getStmts().addAll(if2.getThenStmts().getStmts());

        StmtList stmtList = (StmtList) if2.getParentNode();
        stmtList.getStmts().remove(if2);
        return refactored;
    }

    /**
     * The if statements can be merged, if the second if, follows the first directly and and both have the same condition.
     */
    @Override
    public boolean preCondition() {
        if (!(if2.getParentNode() instanceof StmtList)) {
            throw new IllegalArgumentException("PreCondition failed due to wrong parent access");
        }
        StmtList stmtList = (StmtList) if2.getParentNode();
        return stmtList.getStmts().indexOf(if1) + 1 == stmtList.getStmts().indexOf(if2)
                && if1.getBoolExpr().equals(if2.getBoolExpr());
    }

    @Override
    public boolean postCondition() {
        if (!(if2.getParentNode() instanceof StmtList)) {
            throw new IllegalArgumentException("PostCondition failed due to wrong parent access");
        }
        return !if2.getThenStmts().hasStatements() && !((StmtList) if2.getParentNode()).getStmts().contains(if2);
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
