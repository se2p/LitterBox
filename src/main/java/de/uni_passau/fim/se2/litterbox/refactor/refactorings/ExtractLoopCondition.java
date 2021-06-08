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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ExtractLoopCondition extends CloneVisitor implements Refactoring {

    public static final String NAME = "extract_loop_condition";

    private final RepeatForeverStmt foreverLoop;
    private final IfThenStmt ifThenStmt;
    private final List<Stmt> replacement = new ArrayList<>();

    public ExtractLoopCondition(RepeatForeverStmt foreverLoop, IfThenStmt ifThenStmt) {
        this.foreverLoop = Preconditions.checkNotNull(foreverLoop);
        this.ifThenStmt = Preconditions.checkNotNull(ifThenStmt);
        TerminationStmt stopStmt = (TerminationStmt) ifThenStmt.getThenStmts().getStatement(0);

        List<Stmt> remainingStmts = foreverLoop.getStmtList().getStmts().stream().filter(s -> s != ifThenStmt).collect(Collectors.toList());

        UntilStmt replacementLoop = new UntilStmt(apply(ifThenStmt.getBoolExpr()), new StmtList(applyList(remainingStmts)), apply(foreverLoop.getMetadata()));
        replacement.add(replacementLoop);
        if (!(stopStmt instanceof StopThisScript)) {
            replacement.add(apply(stopStmt));
        }
    }

    @Override
    public Program apply(Program program) {
        return (Program) program.accept(new StatementReplacementVisitor(foreverLoop, replacement));
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String toString() {
        return NAME + System.lineSeparator() + "Replaced loop:" + System.lineSeparator() + foreverLoop.getScratchBlocks() + System.lineSeparator() +
                "with until loop:" + System.lineSeparator() + replacement.get(0).getScratchBlocks() +  System.lineSeparator();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExtractLoopCondition that = (ExtractLoopCondition) o;
        return Objects.equals(foreverLoop, that.foreverLoop) && Objects.equals(ifThenStmt, that.ifThenStmt) && Objects.equals(replacement, that.replacement);
    }

    @Override
    public int hashCode() {
        return Objects.hash(foreverLoop, ifThenStmt, replacement);
    }
}
