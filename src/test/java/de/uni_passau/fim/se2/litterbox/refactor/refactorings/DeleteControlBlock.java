package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.ControlStmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.List;
import java.util.stream.Collectors;

public class DeleteControlBlock extends CloneVisitor implements Refactoring {

    private final ControlStmt controlStmt;
    private static final String NAME = "delete_control_block";

    public DeleteControlBlock(ControlStmt controlStmt) {
        this.controlStmt = Preconditions.checkNotNull(controlStmt);
    }

    @Override
    public Program apply(Program program) {
        return (Program) program.accept(this);
    }

    @Override
    public ASTNode visit(StmtList stmtList) {
        List<Stmt> statements = stmtList.getStmts().stream().filter(s -> s != this.controlStmt).collect(Collectors.toList());
        return new StmtList(applyList(statements));
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String toString() {
        return NAME + "(" + controlStmt.getUniqueName() + ")";
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof DeleteControlBlock)) {
            return false;
        }
        return controlStmt.equals(((DeleteControlBlock) other).controlStmt) ;
    }

    @Override
    public int hashCode() {
        return controlStmt.hashCode();
    }

}
