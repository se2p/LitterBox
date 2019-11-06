package scratch.newast.model.statement.spritemotion;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;
import scratch.newast.model.expression.num.NumExpr;

public class SetYTo implements SpriteMotionStmt {

    private final NumExpr num;
    private final ImmutableList<ASTNode> children;

    public SetYTo(NumExpr num) {
        this.num = num;
        children = ImmutableList.<ASTNode>builder().add(num).build();
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

