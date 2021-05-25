package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatTimesStmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.StatementReplacementVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LoopUnrolling extends CloneVisitor implements Refactoring {

    public static final String NAME = "loop_unrolling";

    private final RepeatTimesStmt loop;
    private final int value;
    private final List<Stmt> unrolledStmts;

    public LoopUnrolling(RepeatTimesStmt loop, int value) {
        this.loop = Preconditions.checkNotNull(loop);
        this.value = value;
        unrolledStmts = new ArrayList<>();

        for (int i = 0; i < value; i++) {
            unrolledStmts.addAll(apply(loop.getStmtList()).getStmts());
        }
    }

    @Override
    public Program apply(Program program) {
        return (Program) program.accept(new StatementReplacementVisitor(loop, unrolledStmts));
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String toString() {
        return NAME + System.lineSeparator() + "Unrolled loop:" + System.lineSeparator() + loop.getScratchBlocks() + System.lineSeparator() +
                this.value + " times" +  System.lineSeparator();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LoopUnrolling)) return false;
        LoopUnrolling that = (LoopUnrolling) o;
        return value == that.value && Objects.equals(loop, that.loop) && Objects.equals(unrolledStmts, that.unrolledStmts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(loop, value, unrolledStmts);
    }
}
