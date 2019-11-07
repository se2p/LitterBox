package scratch.newast.model.expression.num;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;
import scratch.newast.model.numfunct.NumFunct;

public class NumFunctOf implements NumExpr {
    private NumFunct funct;
    private NumExpr num;
    private final ImmutableList<ASTNode> children;

    public NumFunctOf(NumFunct funct, NumExpr num) {
        this.funct = funct;
        this.num = num;
        children = ImmutableList.<ASTNode>builder().add(funct).add(num).build();
    }

    public NumFunct getFunct() {
        return funct;
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