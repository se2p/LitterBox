package scratch.ast.model.touchable;

import scratch.ast.model.AbstractNode;
import scratch.ast.model.expression.string.StringExpr;
import scratch.ast.visitor.ScratchVisitor;

public class SpriteTouchable extends AbstractNode implements Touchable {

    private final StringExpr stringExpr;

    public SpriteTouchable(StringExpr stringExpr) {
        super(stringExpr);
        this.stringExpr = stringExpr;
    }

    public StringExpr getStringExpr() {
        return stringExpr;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }


}
