package scratch.newast.model.statement.declaration;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;
import scratch.newast.model.expression.string.StringExpr;
import scratch.newast.model.type.Type;

public class DeclarationAttributeAsTypeStmt implements DeclarationStmt{
    private StringExpr stringExpr;
    private Type type;
    private final ImmutableList<ASTNode> children;

    public DeclarationAttributeAsTypeStmt(StringExpr stringExpr, Type type) {
        this.stringExpr = stringExpr;
        this.type = type;
        ImmutableList.Builder<ASTNode> builder = ImmutableList.builder();
        children = builder.build();
    }

    public StringExpr getStringExpr() {
        return stringExpr;
    }

    public void setStringExpr(StringExpr stringExpr) {
        this.stringExpr = stringExpr;
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
