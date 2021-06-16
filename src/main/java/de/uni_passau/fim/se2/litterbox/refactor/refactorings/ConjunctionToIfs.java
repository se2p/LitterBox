package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.And;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.OnlyCodeCloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.StatementReplacementVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.Arrays;
import java.util.Objects;

/*
If A && B:
  C

to

If A:
  If B:
    C
 */
public class ConjunctionToIfs extends OnlyCodeCloneVisitor implements Refactoring {

    public static final String NAME = "conjunction_to_ifs";

    private final IfThenStmt ifStatement;
    private final IfThenStmt replacementIf;

    public ConjunctionToIfs(IfThenStmt ifStatement) {
        this.ifStatement = Preconditions.checkNotNull(ifStatement);
        And conjunction = (And) ifStatement.getBoolExpr();

        IfThenStmt innerIf = new IfThenStmt(apply(conjunction.getOperand2()),
                apply(ifStatement.getThenStmts()), apply(ifStatement.getMetadata()));

        replacementIf = new IfThenStmt(apply(conjunction.getOperand1()),
                new StmtList(Arrays.asList(innerIf)),
                apply(ifStatement.getMetadata()));
    }

    @Override
    public Program apply(Program program) {
        return (Program) program.accept(new StatementReplacementVisitor(ifStatement, replacementIf));
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String toString() {
        return NAME + System.lineSeparator() + "Replaced if:" + System.lineSeparator() + ifStatement.getScratchBlocks() + System.lineSeparator() +
                "Replacement if:" + System.lineSeparator() + replacementIf.getScratchBlocks() +  System.lineSeparator();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConjunctionToIfs)) return false;
        ConjunctionToIfs that = (ConjunctionToIfs) o;
        return Objects.equals(ifStatement, that.ifStatement) && Objects.equals(replacementIf, that.replacementIf);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ifStatement, replacementIf);
    }
}
