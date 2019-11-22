package scratch.ast.model.literals;

import scratch.ast.model.ASTLeaf;
import scratch.ast.model.AbstractNode;
import scratch.ast.model.expression.color.ColorExpression;
import scratch.ast.visitor.ScratchVisitor;

public class ColorLiteral extends AbstractNode implements ColorExpression, ASTLeaf {

    private final long red;
    private final long green;
    private final long blue;

    public ColorLiteral(long red, long green, long blue) {
        super();
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public long getRed() {
        return red;
    }

    public long getBlue() {
        return blue;
    }

    public long getGreen() {
        return green;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }
}
