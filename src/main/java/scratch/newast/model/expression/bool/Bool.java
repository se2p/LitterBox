package scratch.newast.model.expression.bool;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTLeaf;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;

public class Bool implements BoolExpr, ASTLeaf {

    private final ImmutableList<ASTNode> children;
    private boolean bool;

    public Bool(boolean bool) {
        this.bool = bool;
        children = ImmutableList.<ASTNode>builder().build();
    }

    public boolean isBool() {
        return bool;
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
        String[] returnArray = {""+bool};
        return returnArray;
    }
}
