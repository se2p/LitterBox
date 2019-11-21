package scratch.ast.model.expression;

import scratch.ast.model.ASTNode;
import scratch.ast.model.AbstractNode;

public abstract class BinaryExpression<A extends ASTNode, B extends ASTNode> extends AbstractNode {

    private final A operand1;
    private final B operand2;

    protected BinaryExpression(A operand1, B operand2) {
        super(operand1, operand2);
        this.operand1 = operand1;
        this.operand2 = operand2;
    }

    public A getOperand1() {
        return operand1;
    }

    public B getOperand2() {
        return operand2;
    }

}
