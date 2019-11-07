package scratch.newast.model.expression.num;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;
import scratch.newast.model.expression.string.StringExpr;

public class LengthOfString implements NumExpr {

    private final StringExpr stringExpr;
    private final ImmutableList<ASTNode> children;


    public LengthOfString(StringExpr stringExpr) {
        this.stringExpr = stringExpr;
        children = ImmutableList.<ASTNode>builder().add(stringExpr).build();
    }

    public StringExpr getStringExpr() {
        return stringExpr;
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