package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.And;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.StatementReplacementVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

/*
If A:
  If B:
    C

to

If A && B:
  C
 */
public class IfsToConjunction extends CloneVisitor implements Refactoring {

    public static final String NAME = "ifs_to_conjunction";

    private final IfThenStmt if1;
    private final IfThenStmt if2;
    private final IfThenStmt replacement;

    public IfsToConjunction(IfThenStmt if1, IfThenStmt if2) {
        this.if1 = Preconditions.checkNotNull(if1);
        this.if2 = Preconditions.checkNotNull(if2);

        And conjunction = new And(
                apply(if1.getBoolExpr()),
                apply(if2.getBoolExpr()),
                apply(if2.getMetadata()));

        replacement = new IfThenStmt(conjunction,
                apply(if2.getThenStmts()),
                apply(if1.getMetadata()));
    }

    @Override
    public Program apply(Program program) {
        return (Program) program.accept(new StatementReplacementVisitor(if1, replacement));
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String toString() {
        return NAME + System.lineSeparator() + "Replaced if1:" + System.lineSeparator() + if1.getScratchBlocks() + System.lineSeparator() +
                "Replaced if 2:" + System.lineSeparator() + if2.getScratchBlocks() +  System.lineSeparator() +
                "Replacement:" + System.lineSeparator() + replacement.getScratchBlocks() +  System.lineSeparator();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof IfsToConjunction)) {
            return false;
        }
        return if1.equals(((IfsToConjunction) other).if1)
                && if2.equals(((IfsToConjunction) other).if2);
    }

    @Override
    public int hashCode() {
        return if1.hashCode() + if2.hashCode();
    }
}
