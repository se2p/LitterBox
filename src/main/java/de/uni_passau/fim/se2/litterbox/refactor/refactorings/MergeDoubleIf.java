package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.OnlyCodeCloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.StatementReplacementVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.Arrays;
import java.util.List;

/*
if A:
  B
if A:
  C

to

if A:
  B
  C
 */
public class MergeDoubleIf extends OnlyCodeCloneVisitor implements Refactoring {

    private final IfThenStmt if1;
    private final IfThenStmt if2;
    private final IfThenStmt replacement;
    public static final String NAME = "merge_double_if";

    public MergeDoubleIf(IfThenStmt if1, IfThenStmt if2) {
        this.if1 = Preconditions.checkNotNull(if1);
        this.if2 = Preconditions.checkNotNull(if2);

        List<Stmt> mergedListOfStmts = apply(if1.getThenStmts()).getStmts();
        mergedListOfStmts.addAll(apply(if2.getThenStmts()).getStmts());
        StmtList mergedThenStmts = new StmtList(mergedListOfStmts);
        replacement = new IfThenStmt(apply(if1.getBoolExpr()), mergedThenStmts, apply(if1.getMetadata()));
    }

    @Override
    public Program apply(Program program) {
        return (Program) program.accept(new StatementReplacementVisitor(if1, Arrays.asList(if2), Arrays.asList(replacement)));
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String toString() {
        String if1ScratchBlocks = if1.getScratchBlocks();
        String if2ScratchBlocks = if2.getScratchBlocks();
        String replacementScratchBlocks = replacement.getScratchBlocks();
        return NAME + System.lineSeparator()+"Replaced ifs:"+System.lineSeparator()+System.lineSeparator() + if1ScratchBlocks + System.lineSeparator() + if2ScratchBlocks + System.lineSeparator()+"Replacement:"+System.lineSeparator()+System.lineSeparator() + replacementScratchBlocks;
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
