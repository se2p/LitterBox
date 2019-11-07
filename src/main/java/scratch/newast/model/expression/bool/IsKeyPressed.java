package scratch.newast.model.expression.bool;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.Key;
import scratch.newast.model.ScratchVisitor;

public class IsKeyPressed implements BoolExpr {

    private final Key key;
    private final ImmutableList<ASTNode> children;


    public IsKeyPressed(Key key) {
        this.key = key;
        children = ImmutableList.<ASTNode>builder().add(key).build();
    }

    public Key getKey() {
        return key;
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