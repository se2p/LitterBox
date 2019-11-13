package scratch.newast.model.statement;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;
import scratch.newast.model.expression.list.ExpressionList;
import scratch.newast.model.variable.Identifier;

public class CallStmt implements Stmt {
    private final ImmutableList<ASTNode> children;
    private Identifier ident;
    private ExpressionList expressions;

    public CallStmt(Identifier ident, ExpressionList expressions) {
        this.ident = ident;
        this.expressions = expressions;
        ImmutableList.Builder<ASTNode> builder = ImmutableList.builder();
        children = builder.build();
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public ImmutableList<ASTNode> getChildren() {
        return children;
    }

    public Identifier getIdent() {
        return ident;
    }

    public void setIdent(Identifier ident) {
        this.ident = ident;
    }

    public ExpressionList getExpressions() {
        return expressions;
    }

    public void setExpressions(ExpressionList expressions) {
        this.expressions = expressions;
    }
}
