package scratch.newast.model.expression.num;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTLeaf;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;

public class Number implements NumExpr, ASTLeaf {

    private final float value;
    private final ImmutableList<ASTNode> children;

    public Number(float value) {
        this.value = value;
        children = ImmutableList.<ASTNode>builder().build();
    }

    public float getValue() {
        return value;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public ImmutableList<ASTNode> getChildren() {
        return children;
    }

    @Override
    public String[] toSimpleStringArray() {
        String[] returnArray = {""+value};
        return returnArray;
    }
}