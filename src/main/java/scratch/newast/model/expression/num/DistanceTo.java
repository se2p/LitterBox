package scratch.newast.model.expression.num;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;
import scratch.newast.model.position.Position;

public class DistanceTo implements NumExpr {
    private final Position position;
    private final ImmutableList<ASTNode> children;

    public DistanceTo(Position position) {
        this.position = position;
        children = ImmutableList.<ASTNode>builder().add(position).build();
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