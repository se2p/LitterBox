package scratch.newast.model;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.statement.Stmt;
import scratch.newast.model.type.Type;
import scratch.newast.model.variable.Identifier;

public class DeclarationStmt implements Stmt {

    private Identifier ident;
    private Type type;
    private final ImmutableList<ASTNode> children;

    public DeclarationStmt(Identifier ident, Type type) {
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