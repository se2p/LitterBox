package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopThisScript;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.TerminationStmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.StatementReplacementVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.List;

public class InlineLoopCondition extends CloneVisitor implements Refactoring {

    public static final String NAME = "inline_loop_condition";

    private final UntilStmt untilLoop;
    private final TerminationStmt terminationStmt;
    private final RepeatForeverStmt replacementLoop;

    public InlineLoopCondition(UntilStmt untilLoop) {
        this(untilLoop, null);
    }

    public InlineLoopCondition(UntilStmt untilLoop, TerminationStmt terminationStmt) {
        this.untilLoop = Preconditions.checkNotNull(untilLoop);
        this.terminationStmt = terminationStmt == null ? new StopThisScript(apply(untilLoop.getMetadata())) : terminationStmt;

        IfThenStmt ifThenStmt = new IfThenStmt(apply(untilLoop.getBoolExpr()), new StmtList(this.terminationStmt), apply(untilLoop.getMetadata()));
        List<Stmt> loopBody = apply(untilLoop.getStmtList()).getStmts();
        loopBody.add(ifThenStmt);

        replacementLoop = new RepeatForeverStmt(new StmtList(loopBody), apply(untilLoop.getMetadata()));
    }

    @Override
    public Program apply(Program program) {
        return (Program) program.accept(new StatementReplacementVisitor(untilLoop, replacementLoop));
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String toString() {
        return NAME + System.lineSeparator() + "Replaced until loop:" + System.lineSeparator() + untilLoop.getScratchBlocks() + System.lineSeparator() +
                "with forever loop:" + System.lineSeparator() + replacementLoop.getScratchBlocks() +  System.lineSeparator();
    }
}
