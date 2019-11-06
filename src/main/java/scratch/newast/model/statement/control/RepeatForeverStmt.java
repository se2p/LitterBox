package scratch.newast.model.statement.control;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;
import scratch.newast.model.StmtList;

public class RepeatForeverStmt implements ControlStmt {

    private final StmtList stmtList;
    private final ImmutableList<ASTNode> children;

    public RepeatForeverStmt(StmtList stmtList) {
        this.stmtList = stmtList;
        children = ImmutableList.<ASTNode>builder().add(stmtList).build();
    }

    public StmtList getStmtList() {
        return stmtList;
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
