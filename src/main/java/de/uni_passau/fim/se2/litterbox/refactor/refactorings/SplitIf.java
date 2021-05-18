package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SplitIf extends CloneVisitor implements Refactoring {

    private final IfThenStmt ifThenStmt;
    private final IfThenStmt replacementIf1;
    private final IfThenStmt replacementIf2;
    public static final String NAME = "split_if";

    public SplitIf(IfThenStmt if1) {
        this.ifThenStmt = Preconditions.checkNotNull(if1);
        Preconditions.checkArgument(ifThenStmt.getThenStmts().getNumberOfStatements() > 1);

        List<Stmt> remainingStatements = apply(if1.getThenStmts()).getStmts();
        remainingStatements.remove(0);

        StmtList subStatements1 = new StmtList(apply(if1.getThenStmts().getStatement(0)));
        StmtList subStatements2 = new StmtList(remainingStatements);

        replacementIf1 = new IfThenStmt(apply(if1.getBoolExpr()), subStatements1, apply(if1.getMetadata()));
        replacementIf2 = new IfThenStmt(apply(if1.getBoolExpr()), subStatements2, apply(if1.getMetadata()));
    }

    @Override
    public Program apply(Program program) {
        return (Program) program.accept(this);
    }

    @Override
    public ASTNode visit(StmtList node) {
        List<Stmt> statements = new ArrayList<>();
        for (Stmt stmt : node.getStmts()) {
            if (stmt == ifThenStmt) {
                statements.add(replacementIf1);
                statements.add(replacementIf2);
            } else if (stmt != ifThenStmt) {
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
        if (!(o instanceof SplitIf)) return false;
        SplitIf splitIf = (SplitIf) o;
        return Objects.equals(ifThenStmt, splitIf.ifThenStmt) && Objects.equals(replacementIf1, splitIf.replacementIf1) && Objects.equals(replacementIf2, splitIf.replacementIf2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ifThenStmt, replacementIf1, replacementIf2);
    }
}
