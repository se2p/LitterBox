package scratch.newast.model.statement;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;
import scratch.newast.model.expression.Expression;

public class ExpressionStmt implements Stmt {

    private final Expression expression;
    private final ImmutableList<ASTNode> children;


    public ExpressionStmt(Expression expression) {
        this.expression = expression;
        children = ImmutableList.<ASTNode>builder().add(expression).build();
    }

    public Expression getExpression() {
        return expression;
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
