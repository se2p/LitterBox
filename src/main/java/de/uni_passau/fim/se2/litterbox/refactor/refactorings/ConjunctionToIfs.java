package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.And;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.NodeReplacementVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.Arrays;
import java.util.Objects;

public class ConjunctionToIfs extends CloneVisitor implements Refactoring {

    public static final String NAME = "conjunction_to_ifs";

    private final IfThenStmt ifStatement;
    private final IfThenStmt replacementIf;

    public ConjunctionToIfs(IfThenStmt ifStatement) {
        this.ifStatement = Preconditions.checkNotNull(ifStatement);
        And conjunction = (And) ifStatement.getBoolExpr();

        CloneVisitor cloneVisitor = new CloneVisitor();
        IfThenStmt innerIf = new IfThenStmt(cloneVisitor.apply(conjunction.getOperand2()),
                cloneVisitor.apply(ifStatement.getThenStmts()), ifStatement.getMetadata());

        replacementIf = new IfThenStmt(cloneVisitor.apply(conjunction.getOperand1()),
                new StmtList(Arrays.asList(innerIf)),
                cloneVisitor.apply(ifStatement.getMetadata()));
    }

    @Override
    public Program apply(Program program) {
        NodeReplacementVisitor replacementVisitor =  new NodeReplacementVisitor(ifStatement, replacementIf);
        return (Program) program.accept(replacementVisitor);
    }

    @Override
    public String getName() {
        return NAME;
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
