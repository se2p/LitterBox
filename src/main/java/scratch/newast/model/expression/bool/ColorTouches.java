package scratch.newast.model.expression.bool;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;
import scratch.newast.model.color.Color;

public class ColorTouches implements BoolExpr {
    private final Color first;
    private final Color second;
    private final ImmutableList<ASTNode> children;

    public ColorTouches(Color first, Color second) {
        this.first = first;
        this.second = second;
        children = ImmutableList.<ASTNode>builder().add(first).add(second).build();
    }

    public Color getFirst() {
        return first;
    }

    public Color getSecond() {
        return second;
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