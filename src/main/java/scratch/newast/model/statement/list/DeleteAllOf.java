package scratch.newast.model.statement.list;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;
import scratch.newast.model.variable.Variable;

public class DeleteAllOf implements ListStmt {
    private final Variable variable;
    private final ImmutableList<ASTNode> children;

    public DeleteAllOf(Variable variable) {
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