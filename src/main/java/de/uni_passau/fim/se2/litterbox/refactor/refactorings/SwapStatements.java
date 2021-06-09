package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SwapStatements extends CloneVisitor implements Refactoring {

    public static final String NAME = "swap_statements";

    private final Stmt stmt1;
    private final Stmt stmt2;

    private final Stmt replacementStmt1;
    private final Stmt replacementStmt2;

    public SwapStatements(Stmt stmt1, Stmt stmt2) {
        this.stmt1 = Preconditions.checkNotNull(stmt1);
        this.stmt2 = Preconditions.checkNotNull(stmt2);
        this.replacementStmt1 = apply(stmt1);
        this.replacementStmt2 = apply(stmt2);
    }

    @Override
    public Program apply(Program program) {
        return (Program) program.accept(this);
    }


    @Override
    public ASTNode visit(StmtList node) {
        List<Stmt> statements = new ArrayList<>();
        for (Stmt stmt : node.getStmts()) {
            if (stmt == stmt1) {
                statements.add(replacementStmt2);
            } else if (stmt == stmt2) {
                statements.add(replacementStmt1);
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
    public String toString() {
        return NAME + System.lineSeparator() + "Swapping statements:" + System.lineSeparator() + stmt1.getScratchBlocks() + System.lineSeparator() +
                "and:" + System.lineSeparator() + stmt2.getScratchBlocks() +  System.lineSeparator();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SwapStatements)) return false;
        SwapStatements that = (SwapStatements) o;
        return Objects.equals(stmt1, that.stmt1) && Objects.equals(stmt2, that.stmt2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stmt1, stmt2);
    }
}
