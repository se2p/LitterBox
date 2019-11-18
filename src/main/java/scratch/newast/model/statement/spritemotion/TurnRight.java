package scratch.newast.model.statement.spritemotion;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;
import scratch.newast.model.expression.num.NumExpr;

public class TurnRight implements SpriteMotionStmt {
    private final NumExpr degrees;
    private final ImmutableList<ASTNode> children;

    public TurnRight(NumExpr degrees) {
        this.degrees = degrees;
        children = ImmutableList.<ASTNode>builder().add(degrees).build();
    }

    public NumExpr getDegrees() {
        return degrees;
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