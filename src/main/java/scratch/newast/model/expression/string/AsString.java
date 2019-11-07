package scratch.newast.model.expression.string;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;
import scratch.newast.model.expression.num.NumExpr;

public class AsString implements StringExpr {

    private final NumExpr numExpr;
    private final ImmutableList<ASTNode> children;

    public AsString(NumExpr numExpr) {
        this.numExpr = numExpr;
        ImmutableList.Builder<ASTNode> builder = ImmutableList.builder();
        children = builder.add(numExpr).build();
    }

    public NumExpr getNumExpr() {
        return numExpr;
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