package scratch.newast.model.statement.common;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;
import scratch.newast.model.expression.Expression;
import scratch.newast.model.expression.string.StringExpr;

public class SetAttributeTo implements SetStmt {

    private final ImmutableList<ASTNode> children;
    private StringExpr stringExpr;
    private Expression expr;

    public SetAttributeTo(StringExpr stringExpr, Expression expr) {
        this.stringExpr = stringExpr;
        this.expr = expr;
        children = ImmutableList.<ASTNode>builder().add(stringExpr).add(expr).build();
    }

    public StringExpr getStringExpr() {
        return stringExpr;
    }

    public Expression getExpr() {
        return expr;
    }

    public void setExpr(Expression expr) {
        this.expr = expr;
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