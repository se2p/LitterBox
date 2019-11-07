package scratch.newast.model.statement.spritelook;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;
import scratch.newast.model.expression.num.NumExpr;

public class SetSizeTo implements SpriteLookStmt {

    private final NumExpr percent;
    private final ImmutableList<ASTNode> children;

    public SetSizeTo(NumExpr percent) {
        this.percent = percent;
        children = ImmutableList.<ASTNode>builder().add(percent).build();
    }

    public NumExpr getPercent() {
        return percent;
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