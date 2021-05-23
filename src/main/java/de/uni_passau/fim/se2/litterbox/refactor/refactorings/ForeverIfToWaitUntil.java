package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitUntil;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.StatementReplacementVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.List;
import java.util.Objects;

public class ForeverIfToWaitUntil extends CloneVisitor implements Refactoring {

    public static final String NAME = "forever_if_to_wait_until";

    private final RepeatForeverStmt loop;
    private final RepeatForeverStmt replacementLoop;

    public ForeverIfToWaitUntil(RepeatForeverStmt loop) {
        this.loop = Preconditions.checkNotNull(loop);
        Preconditions.checkArgument(loop.getStmtList().getNumberOfStatements() == 1);
        Preconditions.checkArgument(loop.getStmtList().getStatement(0) instanceof IfThenStmt);

        IfThenStmt ifThenStmt = (IfThenStmt) loop.getStmtList().getStatement(0);
        WaitUntil waitUntil = new WaitUntil(apply(ifThenStmt.getBoolExpr()), apply(ifThenStmt.getMetadata()));

        List<Stmt> loopBody = apply(ifThenStmt.getThenStmts()).getStmts();
        loopBody.add(0, waitUntil);

        replacementLoop = new RepeatForeverStmt(new StmtList(loopBody), apply(loop.getMetadata()));
    }

    @Override
    public Program apply(Program program) {
        return (Program) program.accept(new StatementReplacementVisitor(loop, replacementLoop));
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String toString() {
        return NAME + System.lineSeparator() + "Replaced forever loop:" + System.lineSeparator() + loop.getScratchBlocks() + System.lineSeparator() +
                "with forever-wait loop:" + System.lineSeparator() + replacementLoop.getScratchBlocks() +  System.lineSeparator();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ForeverIfToWaitUntil that = (ForeverIfToWaitUntil) o;
        return Objects.equals(loop, that.loop) && Objects.equals(replacementLoop, that.replacementLoop);
    }

    @Override
    public int hashCode() {
        return Objects.hash(loop, replacementLoop);
    }
}
