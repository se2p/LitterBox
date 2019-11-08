package scratch.newast.model.procedure;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;
import scratch.newast.model.type.Type;
import scratch.newast.model.variable.Identifier;

public class Parameter implements ASTNode {
    private Identifier ident;
    private Type type;
    private final ImmutableList<ASTNode> children;
    private String value;

    public Parameter(Identifier ident, Type type) {
        this.ident = ident;
        this.type = type;
        ImmutableList.Builder<ASTNode> builder = ImmutableList.builder();
        children = builder.build();
    }

    public Identifier getIdent() {
        return ident;
    }

    public Type getType() {
        return type;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public ImmutableList<ASTNode> getChildren() {
        return children;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
