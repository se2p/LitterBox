package scratch.newast.model.statement.spritelook;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;
import scratch.newast.model.expression.num.NumExpr;

public class ChangeLayerBy implements SpriteLookStmt {
    private final NumExpr num;
    private final ImmutableList<ASTNode> children;

    public ChangeLayerBy(NumExpr num) {
        this.num = num;
        ImmutableList.Builder<ASTNode> builder = ImmutableList.builder();
        children = builder.add(num).build();
    }

    public NumExpr getNum() {
        return num;
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