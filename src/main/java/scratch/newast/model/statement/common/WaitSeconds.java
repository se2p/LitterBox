package scratch.newast.model.statement.common;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;
import scratch.newast.model.expression.num.NumExpr;

public class WaitSeconds implements CommonStmt {
    private NumExpr seconds;
    private final ImmutableList<ASTNode> children;

    public WaitSeconds(NumExpr seconds) {
        this.seconds = seconds;
        children = ImmutableList.<ASTNode>builder().add(seconds).build();
    }

    public NumExpr getSeconds() {
        return seconds;
    }

    public void setSeconds(NumExpr seconds) {
        this.seconds = seconds;
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