package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

public class MergeDoubleIf implements Refactoring {

    private final IfThenStmt if1;
    private final IfThenStmt if2;
    private final String if1Initial;
    private final String if2Initial;
    private String refactoredScript;
    private static final String NAME = "merge_double_if";

    public MergeDoubleIf(IfThenStmt if1, IfThenStmt if2) {
        this.if1 = Preconditions.checkNotNull(if1);
        this.if2 = Preconditions.checkNotNull(if2);
        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor();
        if1.accept(visitor);
        if1Initial = visitor.getScratchBlocks();
        visitor = new ScratchBlocksVisitor();
        if2.accept(visitor);
        if2Initial = visitor.getScratchBlocks();
    }

    @Override
    public Program apply(Program program) {
        if1.getThenStmts().getStmts().addAll(if2.getThenStmts().getStmts());
        StmtList stmtList = (StmtList) if2.getParentNode();
        stmtList.getStmts().remove(if2);

        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor();
        if1.getParentNode().accept(visitor);
        refactoredScript = visitor.getScratchBlocks();

        return program;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String toString() {
        return NAME + "\nMerged ifs:\n\n" + if1Initial + "\n" + if2Initial + "\n\nRefactored stmt list:\n\n" + refactoredScript;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof MergeDoubleIf)) {
            return false;
        }
        return if1.equals(((MergeDoubleIf) other).if1)
                && if2.equals(((MergeDoubleIf) other).if2);
    }

    @Override
    public int hashCode() {
        return if1.hashCode() + if2.hashCode();
    }
}
