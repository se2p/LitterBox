package scratch.newast.model.statement.spritelook;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;
import scratch.newast.model.expression.num.NumExpr;

public class GoToLayer implements SpriteLookStmt {

    private final NumExpr layer;
    private final ImmutableList<ASTNode> children;

    public GoToLayer(NumExpr layer) {
        this.layer = layer;
        children = ImmutableList.<ASTNode>builder().add(layer).build();
    }

    public NumExpr getLayer() {
        return layer;
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