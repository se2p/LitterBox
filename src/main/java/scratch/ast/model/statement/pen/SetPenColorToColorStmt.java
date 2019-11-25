package scratch.ast.model.statement.pen;

import scratch.ast.model.AbstractNode;
import scratch.ast.model.expression.color.ColorExpression;
import scratch.ast.visitor.ScratchVisitor;
import utils.Preconditions;

public class SetPenColorToColorStmt extends AbstractNode implements PenStmt {
    private ColorExpression colorExpression;

    public SetPenColorToColorStmt(ColorExpression colorExpression) {
        super(colorExpression);
        this.colorExpression = Preconditions.checkNotNull(colorExpression);
    }

    public ColorExpression getColorExpr() {
        return colorExpression;
    }


    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }
}
