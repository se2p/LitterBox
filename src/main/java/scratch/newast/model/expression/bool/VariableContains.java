package scratch.newast.model.expression.bool;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;
import scratch.newast.model.expression.Expression;
import scratch.newast.model.expression.string.StringExpr;

public class VariableContains implements BoolExpr {

    private final StringExpr variable;
    private final Expression expr;
    private final ImmutableList<ASTNode> children;

    public VariableContains(StringExpr variable, Expression expr) {
        this.variable = variable;
        this.expr = expr;
        children = ImmutableList.<ASTNode>builder().add(variable).add(expr).build();
    }

    public StringExpr getVariable() {
        return variable;
    }


    public Expression getExpr() {
        return expr;
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