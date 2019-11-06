package scratch.newast.model.statement.list;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;
import scratch.newast.model.expression.num.NumExpr;
import scratch.newast.model.expression.string.StringExpr;
import scratch.newast.model.variable.Variable;

public class ReplaceItem implements ListStmt {

    private final NumExpr index;
    private final Variable variable;
    private final StringExpr string;
    private final ImmutableList<ASTNode> children;

    public ReplaceItem(NumExpr index, Variable variable, StringExpr string) {
        this.index = index;
        this.variable = variable;
        this.string = string;
        Builder<ASTNode> builder = ImmutableList.<ASTNode>builder();
        builder.add(index);
        builder.add(variable);
        builder.add(string);
        children = builder.build();
    }

    public NumExpr getIndex() {
        return index;
    }


    public Variable getVariable() {
        return variable;
    }


    public StringExpr getString() {
        return string;
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