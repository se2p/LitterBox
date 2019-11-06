package scratch.newast.model.event;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.Key;
import scratch.newast.model.ScratchVisitor;

public class KeyPressed implements Event {
    private Key key;
    private final ImmutableList<ASTNode> children;

    public KeyPressed(Key key) {
        this.key = key;
        ImmutableList.Builder<ASTNode> builder = ImmutableList.builder();
        children = builder.add(key).build();
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