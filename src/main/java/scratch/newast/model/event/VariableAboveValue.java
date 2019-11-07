package scratch.newast.model.event;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;
import scratch.newast.model.expression.num.NumExpr;
import scratch.newast.model.variable.Variable;

public class VariableAboveValue implements Event {

    private final Variable variable;
    private final NumExpr value;
    private final ImmutableList<ASTNode> children;

    public VariableAboveValue(Variable variable, NumExpr value) {
        this.variable = variable;
        this.value = value;
        children = ImmutableList.<ASTNode>builder().add(value).add(value).build();
    }

    public Variable getVariable() {
        return variable;
    }


    public NumExpr getValue() {
        return value;
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