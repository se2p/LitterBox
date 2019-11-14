package scratch.newast.model.statement.list;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;
import scratch.newast.model.expression.string.StringExpr;
import scratch.newast.model.variable.Variable;

public class AddTo implements ListStmt {

    private final StringExpr string;
    private final Variable variable;
    private final ImmutableList<ASTNode> children;

    public AddTo(StringExpr string, Variable variable) {
        this.string = string;
        this.variable = variable;
        children = ImmutableList.<ASTNode>builder().add(string).add(variable).build();
    }

    public StringExpr getString() {
        return string;
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