package scratch.newast.model;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.statement.spritelook.ListOfStmt;
import scratch.newast.model.statement.termination.TerminationStmt;

public class StmtList implements ASTNode {

    private final ListOfStmt stmts;
    private final TerminationStmt terminationStmt;
    private final ImmutableList<ASTNode> children;

    public StmtList(ListOfStmt stmts, TerminationStmt terminationStmt) {
        this.stmts = stmts;
        this.terminationStmt = terminationStmt;
        ImmutableList.Builder<ASTNode> builder = ImmutableList.builder();
        builder.add(stmts);
        if (terminationStmt != null) {
            builder.add(terminationStmt);
        }
        this.children = builder.build();
    }

    public ListOfStmt getStmts() {
        return stmts;
    }

    public TerminationStmt getTerminationStmt() {
        return terminationStmt;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public ImmutableList<ASTNode> getChildren() {
        return children;
    }
}
