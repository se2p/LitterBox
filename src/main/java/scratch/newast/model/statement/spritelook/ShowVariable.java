package scratch.newast.model.statement.spritelook;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;
import scratch.newast.model.variable.Variable;

public class ShowVariable implements SpriteLookStmt {

    private final ImmutableList<ASTNode> children;
    private final Variable variable;

    public ShowVariable(Variable variable) {
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