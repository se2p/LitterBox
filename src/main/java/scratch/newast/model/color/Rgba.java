package scratch.newast.model.color;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;
import scratch.newast.model.expression.num.NumExpr;

public class Rgba implements Color {

    private final ImmutableList<ASTNode> children;
    private NumExpr rValue;
    private NumExpr gValue;
    private NumExpr bValue;
    private NumExpr aValue;

    public Rgba(NumExpr rValue, NumExpr gValue, NumExpr bValue, NumExpr aValue) {
        this.rValue = rValue;
        this.gValue = gValue;
        this.bValue = bValue;
        this.aValue = aValue;
        children = ImmutableList.<ASTNode>builder()
            .add(rValue)
            .add(gValue)
            .add(bValue)
            .add(aValue)
            .build();
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
