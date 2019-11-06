package scratch.newast.model.position;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;
import scratch.newast.model.expression.num.Number;

public class CoordinatePosition implements Position {

    private final Number xCoord;
    private final Number yCoord;
    private final ImmutableList<ASTNode> children;

    public CoordinatePosition(Number xCoord, Number yCoord) {
        this.xCoord = xCoord;
        this.yCoord = yCoord;
        children = ImmutableList.<ASTNode>builder().add(xCoord).add(yCoord).build();
    }

    public Number getXCoord() {
        return xCoord;
    }

    public Number getYCoord() {
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