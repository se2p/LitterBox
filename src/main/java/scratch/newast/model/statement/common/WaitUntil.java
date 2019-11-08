package scratch.newast.model.statement.common;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;
import scratch.newast.model.expression.num.NumExpr;

public class WaitUntil implements CommonStmt {
    private NumExpr until;
    private final ImmutableList<ASTNode> children;

    public WaitUntil(NumExpr until) {
        this.until = until;
        children = ImmutableList.<ASTNode>builder().add(until).build();
    }

    public NumExpr getUntil() {
        return until;
    }

    public void setUntil(NumExpr until) {
        this.until = until;
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