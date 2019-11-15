package scratch.newast.model.statement.declaration;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;
import scratch.newast.model.type.Type;
import scratch.newast.model.variable.Identifier;

public class DeclarationIdentAsTypeStmt implements DeclarationStmt {

    private Identifier ident;
    private Type type;
    private final ImmutableList<ASTNode> children;

    public DeclarationIdentAsTypeStmt(Identifier ident, Type type) {
        this.ident = ident;
        this.type = type;
        ImmutableList.Builder<ASTNode> builder = ImmutableList.builder();
        children = builder.build();
    }

    public Identifier getIdent() {
        return ident;
    }

    public void setIdent(Identifier ident) {
        this.ident = ident;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
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