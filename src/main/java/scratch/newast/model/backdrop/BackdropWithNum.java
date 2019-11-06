package scratch.newast.model.backdrop;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;
import scratch.newast.model.expression.num.Number;

public class BackdropWithNum implements Backdrop {
    private final Number num;
    private final ImmutableList<ASTNode> children;

    public BackdropWithNum(Number num) {
        this.num = num;
        children = ImmutableList.<ASTNode>builder().add(num).build();
    }

    public Number getNum() {
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