package scratch.ast.model.expression;

import scratch.ast.model.ASTNode;
import scratch.ast.model.AbstractNode;

public abstract class UnaryExpression<A extends ASTNode> extends AbstractNode {

    private final A operand1;

    protected UnaryExpression(A operand1) {
        super(operand1);
        this.operand1 = operand1;
    }

    public A getOperand1() {
        return operand1;
    }
    
}
