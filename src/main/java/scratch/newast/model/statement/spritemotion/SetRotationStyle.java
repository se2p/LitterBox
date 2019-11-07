package scratch.newast.model.statement.spritemotion;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;
import scratch.newast.model.rotationstyle.RotationStyle;

public class SetRotationStyle implements SpriteMotionStmt {
    private final RotationStyle rotationStyle;
    private final ImmutableList<ASTNode> children;

    public SetRotationStyle(RotationStyle rotationStyle) {
        this.rotationStyle = rotationStyle;
        children = ImmutableList.<ASTNode>builder().add(rotationStyle).build();
    }

    public RotationStyle getRotationStyle() {
        return rotationStyle;
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