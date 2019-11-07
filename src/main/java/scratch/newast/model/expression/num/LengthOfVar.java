package scratch.newast.model.expression.num;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;
import scratch.newast.model.variable.Variable;

public class LengthOfVar implements NumExpr {
    private final Variable variable;
    private final ImmutableList<ASTNode> children;

    public LengthOfVar(Variable variable) {
        this.variable = variable;
        children = ImmutableList.<ASTNode>builder().add(variable).build();
    }

    public Variable getVariable() {
        return variable;
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