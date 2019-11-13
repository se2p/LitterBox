package scratch.newast.model.statement.spritelook;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;
import scratch.newast.model.expression.string.StringExpr;

public class Think implements SpriteLookStmt {
    private final StringExpr thought;
    private final ImmutableList<ASTNode> children;


    public Think(StringExpr thought) {
        this.thought = thought;
        children = ImmutableList.<ASTNode>builder().build();
    }

    public StringExpr getThought() {
        return thought;
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