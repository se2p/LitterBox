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

import java.util.Arrays;
import java.util.List;

public class ForeverWaitToForeverIf extends CloneVisitor implements Refactoring {

    public static final String NAME = "forever_wait_to_forever_if";

    private final RepeatForeverStmt loop;
    private final RepeatForeverStmt replacementLoop;

    public ForeverWaitToForeverIf(RepeatForeverStmt loop) {
        this.loop = Preconditions.checkNotNull(loop);
        Preconditions.checkArgument(loop.getStmtList().getStatement(0) instanceof WaitUntil);

        WaitUntil waitUntil = (WaitUntil) loop.getStmtList().getStatement(0);
        List<Stmt> statements = apply(loop.getStmtList()).getStmts();
        statements.remove(0);

        IfThenStmt ifThenStmt = new IfThenStmt(apply(waitUntil.getUntil()), new StmtList(statements), apply(waitUntil.getMetadata()));
        replacementLoop = new RepeatForeverStmt(new StmtList(Arrays.asList(ifThenStmt)), apply(loop.getMetadata()));
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
        return NAME + System.lineSeparator() + "Replaced forever loop with wait:" + System.lineSeparator() + loop.getScratchBlocks() + System.lineSeparator() +
                "with forever loop with if:" + System.lineSeparator() + replacementLoop.getScratchBlocks() +  System.lineSeparator();
    }
}
