package scratch.newast.model.expression.bool;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;
import scratch.newast.model.touchable.Touchable;

public class Touching implements BoolExpr {

    private final Touchable touchable;
    private final ImmutableList<ASTNode> children;

    public Touching(Touchable touchable) {
        this.touchable = touchable;
        children = ImmutableList.<ASTNode>builder().add(touchable).build();
    }

    public Touchable getTouchable() {
        return touchable;
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