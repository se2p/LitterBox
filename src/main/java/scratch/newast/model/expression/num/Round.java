package scratch.newast.model.expression.num;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;

public class Round implements NumExpr {
    private final NumExpr num;
    private final ImmutableList<ASTNode> children;

    public Round(NumExpr num) {
        this.num = num;
        children = ImmutableList.<ASTNode>builder().add(num).build();
    }

    public NumExpr getNum() {
        return num;
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