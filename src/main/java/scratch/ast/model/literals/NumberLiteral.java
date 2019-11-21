package scratch.ast.model.literals;

import scratch.ast.model.ASTLeaf;
import scratch.ast.model.AbstractNode;
import scratch.ast.model.expression.num.NumExpr;
import scratch.ast.visitor.ScratchVisitor;

public class NumberLiteral extends AbstractNode implements NumExpr, ASTLeaf {

    private final double value;

    NumberLiteral(double number) {
        super();
        this.value = number;
    }

    public double getValue() {
        return value;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }

}
