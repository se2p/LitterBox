package scratch.ast.model.statement.pen;

import scratch.ast.model.AbstractNode;
import scratch.ast.model.expression.num.NumExpr;
import scratch.ast.model.expression.string.StringExpr;
import scratch.ast.visitor.ScratchVisitor;

public class ChangePenColorParamBy extends AbstractNode implements PenStmt {
    private NumExpr value;
    private StringExpr param;

    public ChangePenColorParamBy(NumExpr value, StringExpr param) {
        super(value, param);
        this.value = value;
        this.param = param;
    }

    public NumExpr getValue() {
        return value;
    }

    public StringExpr getParam() {
        return param;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }
}
