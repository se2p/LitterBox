package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Not;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class IfElseToIfIfNot extends CloneVisitor implements Refactoring {

    public static final String NAME = "ifelse_to_ififnot";

    private final IfElseStmt ifElseStmt;
    private final IfThenStmt replacementIf1;
    private final IfThenStmt replacementIf2;

    public IfElseToIfIfNot(IfElseStmt if1) {
        this.ifElseStmt = Preconditions.checkNotNull(if1);

        CloneVisitor cloneVisitor = new CloneVisitor();

        replacementIf1 = new IfThenStmt(cloneVisitor.apply(ifElseStmt.getBoolExpr()),
                cloneVisitor.apply(ifElseStmt.getThenStmts()),
                cloneVisitor.apply(ifElseStmt.getMetadata()));

        replacementIf2 = new IfThenStmt(new Not(cloneVisitor.apply(ifElseStmt.getBoolExpr()),
                ifElseStmt.getMetadata()),
                cloneVisitor.apply(ifElseStmt.getElseStmts()),
                cloneVisitor.apply(ifElseStmt.getMetadata()));
    }

    @Override
    public Program apply(Program program) {
        return (Program) program.accept(this);
    }

    @Override
    public ASTNode visit(StmtList node) {
        List<Stmt> statements = new ArrayList<>();
        for (Stmt stmt : node.getStmts()) {
            if (stmt == ifElseStmt) {
                statements.add(replacementIf1);
                statements.add(replacementIf2);
            } else {
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
        if (!(o instanceof IfElseToIfIfNot)) return false;
        IfElseToIfIfNot that = (IfElseToIfIfNot) o;
        return Objects.equals(ifElseStmt, that.ifElseStmt) && Objects.equals(replacementIf1, that.replacementIf1) && Objects.equals(replacementIf2, that.replacementIf2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ifElseStmt, replacementIf1, replacementIf2);
    }
}
