package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.And;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class IfElseToConjunction extends CloneVisitor implements Refactoring {

    public static final String NAME = "ifelse_to_conjunction";

    private final IfThenStmt if1;
    private final IfElseStmt if2;
    private final IfThenStmt replacementIf1;
    private final IfThenStmt replacementIf2;

    public IfElseToConjunction(IfThenStmt if1) {
        this.if1 = Preconditions.checkNotNull(if1);
        this.if2 = (IfElseStmt) if1.getThenStmts().getStatement(0);

        CloneVisitor cloneVisitor = new CloneVisitor();
        And conjunction = new And(
                cloneVisitor.apply(if1.getBoolExpr()),
                cloneVisitor.apply(if2.getBoolExpr()),
                if2.getMetadata());

        replacementIf1 = new IfThenStmt(conjunction,
                cloneVisitor.apply(if2.getThenStmts()),
                cloneVisitor.apply(if1.getMetadata()));

        replacementIf2 = new IfThenStmt(if1.getBoolExpr(),
                cloneVisitor.apply(if2.getElseStmts()),
                cloneVisitor.apply(if2.getMetadata()));
    }

    @Override
    public Program apply(Program program) {
        return (Program) program.accept(this);
    }

    @Override
    public ASTNode visit(StmtList node) {
        List<Stmt> statements = new ArrayList<>();
        for (Stmt stmt : node.getStmts()) {
            if (stmt == if1) {
                statements.add(replacementIf1);
                statements.add(replacementIf2);
            } else if (stmt != if2) {
                statements.add(apply(stmt));
            }
        }
        return new StmtList(statements);
    }

    @Override
    public String getName() {
        return NAME;
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
