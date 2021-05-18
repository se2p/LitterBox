package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class IfIfNotToIfElse extends CloneVisitor implements Refactoring {

    public static final String NAME = "ifelse_to_ififnot";

    private final IfThenStmt ifThen1;
    private final IfThenStmt ifThen2;
    private final IfElseStmt replacementIf;

    public IfIfNotToIfElse(IfThenStmt if1, IfThenStmt if2) {
        this.ifThen1 = Preconditions.checkNotNull(if1);
        this.ifThen2 = Preconditions.checkNotNull(if2);

        CloneVisitor cloneVisitor = new CloneVisitor();

        replacementIf = new IfElseStmt(cloneVisitor.apply(if1.getBoolExpr()),
                cloneVisitor.apply(ifThen1.getThenStmts()),
                cloneVisitor.apply(ifThen2.getThenStmts()),
                cloneVisitor.apply(ifThen1.getMetadata()));
    }

    @Override
    public Program apply(Program program) {
        return (Program) program.accept(this);
    }

    @Override
    public ASTNode visit(StmtList node) {
        List<Stmt> statements = new ArrayList<>();
        for (Stmt stmt : node.getStmts()) {
            if (stmt == ifThen1) {
                statements.add(replacementIf);
            } else if (stmt != ifThen2){
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
        if (!(o instanceof IfIfNotToIfElse)) return false;
        IfIfNotToIfElse that = (IfIfNotToIfElse) o;
        return Objects.equals(ifThen1, that.ifThen1) && Objects.equals(ifThen2, that.ifThen2) && Objects.equals(replacementIf, that.replacementIf);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ifThen1, ifThen2, replacementIf);
    }
}
