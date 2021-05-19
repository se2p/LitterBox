package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.And;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.StatementReplacementVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.Objects;

/*
If A:
  If B:
    C
  Else:
    D

to

If A && B:
  C
If A:
  D
 */
public class IfElseToConjunction extends CloneVisitor implements Refactoring {

    public static final String NAME = "ifelse_to_conjunction";

    private final IfThenStmt if1;
    private final IfElseStmt if2;
    private final IfThenStmt replacementIf1;
    private final IfThenStmt replacementIf2;

    public IfElseToConjunction(IfThenStmt if1) {
        this.if1 = Preconditions.checkNotNull(if1);
        this.if2 = (IfElseStmt) if1.getThenStmts().getStatement(0);

        And conjunction = new And(
                apply(if1.getBoolExpr()),
                apply(if2.getBoolExpr()),
                apply(if2.getMetadata()));

        replacementIf1 = new IfThenStmt(conjunction,
                apply(if2.getThenStmts()),
                apply(if1.getMetadata()));

        replacementIf2 = new IfThenStmt(apply(if1.getBoolExpr()),
                apply(if2.getElseStmts()),
                apply(if2.getMetadata()));
    }

    @Override
    public Program apply(Program program) {
        return (Program) program.accept(new StatementReplacementVisitor(if1, replacementIf1, replacementIf2));
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String toString() {
        return NAME + System.lineSeparator() + "Replaced if 1:" + System.lineSeparator() + if1.getScratchBlocks() + System.lineSeparator() +
                "Replaced if 2:" + System.lineSeparator() + if2.getScratchBlocks() +  System.lineSeparator() +
                "Replacement if 1:" + System.lineSeparator() + replacementIf1.getScratchBlocks() +  System.lineSeparator() +
                "Replacement if 2:" + System.lineSeparator() + replacementIf2.getScratchBlocks() +  System.lineSeparator();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IfElseToConjunction)) return false;
        IfElseToConjunction that = (IfElseToConjunction) o;
        return Objects.equals(if1, that.if1) && Objects.equals(if2, that.if2) && Objects.equals(replacementIf1, that.replacementIf1) && Objects.equals(replacementIf2, that.replacementIf2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(if1, if2, replacementIf1, replacementIf2);
    }
}
