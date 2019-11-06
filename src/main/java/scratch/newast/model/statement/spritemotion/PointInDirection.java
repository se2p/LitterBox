package scratch.newast.model.statement.spritemotion;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;
import scratch.newast.model.expression.num.NumExpr;

public class PointInDirection implements SpriteMotionStmt {
    private final NumExpr direction;
    private final ImmutableList<ASTNode> children;

    public PointInDirection(NumExpr direction) {
        this.direction = direction;
        children = ImmutableList.<ASTNode>builder().add(direction).build();
    }

    public NumExpr getDirection() {
        return direction;
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