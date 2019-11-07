package scratch.newast.model.sound;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;
import scratch.newast.model.variable.Identifier;

public class SoundWithId implements Sound {

    private final Identifier identifier;
    private final ImmutableList<ASTNode> children;

    public SoundWithId(Identifier identifier) {
        this.identifier = identifier;
        children = ImmutableList.<ASTNode>builder().add(identifier).build();
    }

    public Identifier getIdentifier() {
        return identifier;
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