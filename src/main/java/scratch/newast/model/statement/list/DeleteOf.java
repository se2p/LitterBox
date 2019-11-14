package scratch.newast.model.statement.list;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;
import scratch.newast.model.expression.num.NumExpr;
import scratch.newast.model.variable.Variable;

public class DeleteOf implements ListStmt {
    private final NumExpr num;
    private final Variable variable;
    private final ImmutableList<ASTNode> children;

    public DeleteOf(NumExpr num, Variable variable) {
        this.num = num;
        this.variable = variable;
        children = ImmutableList.<ASTNode>builder().add(num).add(variable).build();
    }

    public NumExpr getNum() {
        return num;
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