package scratch.newast.model.statement.spritemotion;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;
import scratch.newast.model.expression.num.NumExpr;
import scratch.newast.model.position.Position;

public class GlideSecsTo implements SpriteMotionStmt {
    private final NumExpr secs;
    private final Position position;
    private final ImmutableList<ASTNode> children;

    public GlideSecsTo(NumExpr secs, Position position) {
        this.secs = secs;
        this.position = position;
        children = ImmutableList.<ASTNode>builder().add(secs).add(position).build();
    }

    public NumExpr getSecs() {
        return secs;
    }

    public Position getPosition() {
        return position;
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