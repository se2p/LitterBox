package scratch.newast.model.expression.string;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;
import scratch.newast.model.expression.num.NumExpr;

public class LetterOf implements StringExpr {

    private final NumExpr num;
    private final StringExpr stringExpr;
    private final ImmutableList<ASTNode> children;

    public LetterOf(NumExpr num, StringExpr stringExpr) {
        this.num = num;
        this.stringExpr = stringExpr;
        children = ImmutableList.<ASTNode>builder().add(num).add(stringExpr).build();
    }

    public NumExpr getNum() {
        return num;
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