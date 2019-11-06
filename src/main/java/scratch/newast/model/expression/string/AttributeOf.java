package scratch.newast.model.expression.string;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;
import scratch.newast.model.variable.Identifier;

public class AttributeOf implements StringExpr {

    private final ImmutableList<ASTNode> children;
    private final StringExpr attribute;
    private final Identifier identifier;

    public AttributeOf(StringExpr attribute, Identifier identifier) {
        this.attribute = attribute;
        this.identifier = identifier;

        ImmutableList.Builder<ASTNode> builder = ImmutableList.builder();
        children = builder.add(attribute).add(identifier).build();
    }

    public StringExpr getAttribute() {
        return attribute;
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