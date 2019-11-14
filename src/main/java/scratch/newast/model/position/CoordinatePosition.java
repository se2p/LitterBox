package scratch.newast.model.position;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;
import scratch.newast.model.expression.num.NumExpr;

public class CoordinatePosition implements Position {

    private final NumExpr xCoord;
    private final NumExpr yCoord;
    private final ImmutableList<ASTNode> children;

    public CoordinatePosition(NumExpr xCoord, NumExpr yCoord) {
        this.xCoord = xCoord;
        this.yCoord = yCoord;
        children = ImmutableList.<ASTNode>builder().add(xCoord).add(yCoord).build();
    }

    public NumExpr getXCoord() {
        return xCoord;
    }

    public NumExpr getYCoord() {
        return yCoord;
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