package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;
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
if not A
  C

to

If A:
  B
Else:
  C:
 */
public class IfIfNotToIfElse extends CloneVisitor implements Refactoring {

    public static final String NAME = "ififnot_to_ifelse";

    private final IfThenStmt ifThen1;
    private final IfThenStmt ifThen2;
    private final IfElseStmt replacementIf;

    public IfIfNotToIfElse(IfThenStmt if1, IfThenStmt if2) {
        this.ifThen1 = Preconditions.checkNotNull(if1);
        this.ifThen2 = Preconditions.checkNotNull(if2);

        replacementIf = new IfElseStmt(apply(if1.getBoolExpr()),
                apply(ifThen1.getThenStmts()),
                apply(ifThen2.getThenStmts()),
                apply(ifThen1.getMetadata()));
    }

    @Override
    public Program apply(Program program) {
        return (Program) program.accept(new StatementReplacementVisitor(ifThen1, Arrays.asList(ifThen2), Arrays.asList(replacementIf)));
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IfIfNotToIfElse)) return false;
        IfIfNotToIfElse that = (IfIfNotToIfElse) o;
        return Objects.equals(ifThen1, that.ifThen1) && Objects.equals(ifThen2, that.ifThen2) && Objects.equals(replacementIf, that.replacementIf);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ifThen1, ifThen2, replacementIf);
    }
}
