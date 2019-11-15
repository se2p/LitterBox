package scratch.newast.model.statement.common;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;
import scratch.newast.model.expression.Expression;
import scratch.newast.model.expression.string.StringExpr;
import scratch.newast.model.variable.Variable;

public class ChangeAttributeBy implements CommonStmt {

    private final ImmutableList<ASTNode> children;
    private StringExpr attribute;
    private Expression expr;

    public ChangeAttributeBy(StringExpr attribute, Expression expr) {
        this.attribute = attribute;
        this.expr = expr;
        children = ImmutableList.<ASTNode>builder().add(attribute).add(expr).build();
    }

    public StringExpr getAttribute() {
        return attribute;
    }

    public void setAttribute(Variable attribute) {
        this.attribute = attribute;
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