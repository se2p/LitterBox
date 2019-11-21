package scratch.ast.model.literals;

import scratch.ast.model.ASTLeaf;
import scratch.ast.model.AbstractNode;
import scratch.ast.model.expression.string.StringExpr;
import scratch.ast.visitor.ScratchVisitor;

public class StringLiteral extends AbstractNode implements StringExpr, ASTLeaf {

    private final String text;

    StringLiteral(String text) {
        super();
        this.text = text;
    }

    public String getText() {
        return text;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }

}
