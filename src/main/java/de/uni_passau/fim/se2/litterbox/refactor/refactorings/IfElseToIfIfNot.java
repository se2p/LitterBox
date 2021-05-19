package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Not;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.StatementReplacementVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.Arrays;
import java.util.Objects;

/*
If A:
  B
Else:
  C:

to

If A:
  B
if not A
  C
 */
public class IfElseToIfIfNot extends CloneVisitor implements Refactoring {

    public static final String NAME = "ifelse_to_ififnot";

    private final IfElseStmt ifElseStmt;
    private final IfThenStmt replacementIf1;
    private final IfThenStmt replacementIf2;

    public IfElseToIfIfNot(IfElseStmt if1) {
        this.ifElseStmt = Preconditions.checkNotNull(if1);

        replacementIf1 = new IfThenStmt(apply(ifElseStmt.getBoolExpr()),
                apply(ifElseStmt.getThenStmts()),
                apply(ifElseStmt.getMetadata()));

        replacementIf2 = new IfThenStmt(new Not(apply(ifElseStmt.getBoolExpr()),
                apply(ifElseStmt.getMetadata())),
                apply(ifElseStmt.getElseStmts()),
                apply(ifElseStmt.getMetadata()));
    }

    @Override
    public Program apply(Program program) {
        return (Program) program.accept(new StatementReplacementVisitor(ifElseStmt, Arrays.asList(replacementIf1, replacementIf2)));
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IfElseToIfIfNot)) return false;
        IfElseToIfIfNot that = (IfElseToIfIfNot) o;
        return Objects.equals(ifElseStmt, that.ifElseStmt) && Objects.equals(replacementIf1, that.replacementIf1) && Objects.equals(replacementIf2, that.replacementIf2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ifElseStmt, replacementIf1, replacementIf2);
    }
}
