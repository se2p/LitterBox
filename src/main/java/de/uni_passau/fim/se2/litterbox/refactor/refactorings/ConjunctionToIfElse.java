package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.And;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.StatementReplacementVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.Arrays;
import java.util.Objects;

/*
If A && B:
  C
If A:
  D

to

If A:
  If B:
    C
  Else:
    D
 */
public class ConjunctionToIfElse extends CloneVisitor implements Refactoring {

    public static final String NAME = "conjunction_to_ifelse";

    private final IfThenStmt ifStatement1;
    private final IfThenStmt ifStatement2;
    private final IfThenStmt replacementIf;

    public ConjunctionToIfElse(IfThenStmt ifStatement1, IfThenStmt ifStatement2) {
        this.ifStatement1 = Preconditions.checkNotNull(ifStatement1);
        this.ifStatement2 = Preconditions.checkNotNull(ifStatement2);

        And conjunction = (And) ifStatement1.getBoolExpr();
        BoolExpr commonExpression = conjunction.getOperand1().equals(ifStatement2.getBoolExpr()) ? conjunction.getOperand1() : conjunction.getOperand2();
        BoolExpr distinctExpression = conjunction.getOperand1().equals(ifStatement2.getBoolExpr()) ? conjunction.getOperand2() : conjunction.getOperand1();

        IfElseStmt innerIf = new IfElseStmt(apply(distinctExpression),
                apply(ifStatement1.getThenStmts()),
                apply(ifStatement2.getThenStmts()),
                apply(ifStatement2.getMetadata()));

        replacementIf = new IfThenStmt(apply(commonExpression),
                new StmtList(Arrays.asList(innerIf)), apply(ifStatement1.getMetadata()));
    }

    @Override
    public Program apply(Program program) {
        return (Program) program.accept(new StatementReplacementVisitor(ifStatement1, Arrays.asList(ifStatement2), Arrays.asList(replacementIf)));
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String toString() {
        return NAME + System.lineSeparator() + "Replaced if 1:" + System.lineSeparator() + ifStatement1.getScratchBlocks() + System.lineSeparator() +
                "Replaced if 2:" + System.lineSeparator() + ifStatement2.getScratchBlocks() +  System.lineSeparator() +
                "Replacement if:" + System.lineSeparator() + replacementIf.getScratchBlocks() +  System.lineSeparator();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConjunctionToIfElse)) return false;
        ConjunctionToIfElse that = (ConjunctionToIfElse) o;
        return Objects.equals(ifStatement1, that.ifStatement1) && Objects.equals(ifStatement2, that.ifStatement2) && Objects.equals(replacementIf, that.replacementIf);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ifStatement1, ifStatement2, replacementIf);
    }
}
