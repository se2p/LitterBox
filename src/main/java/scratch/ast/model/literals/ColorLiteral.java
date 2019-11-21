package scratch.ast.model.literals;

import scratch.ast.model.ASTLeaf;
import scratch.ast.model.AbstractNode;
import scratch.ast.model.expression.color.ColorExpression;
import scratch.ast.visitor.ScratchVisitor;

public class ColorLiteral extends AbstractNode implements ColorExpression, ASTLeaf {

    private final int red;
    private final int green;
    private final int blue;

    public ColorLiteral(int red, int green, int blue) {
        super();
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }
}
