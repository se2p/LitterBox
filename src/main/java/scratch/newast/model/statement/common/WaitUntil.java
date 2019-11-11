package scratch.newast.model.statement.common;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;
import scratch.newast.model.expression.bool.BoolExpr;

public class WaitUntil implements CommonStmt {

    private BoolExpr until;
    private final ImmutableList<ASTNode> children;

    public WaitUntil(BoolExpr until) {
        this.until = until;
        children = ImmutableList.<ASTNode>builder().add(until).build();
    }

    public BoolExpr getUntil() {
        return until;
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