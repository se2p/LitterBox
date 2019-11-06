package scratch.newast.model.statement.spritelook;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;
import scratch.newast.model.expression.num.NumExpr;
import scratch.newast.model.expression.string.StringExpr;

public class ThinkForSecs implements SpriteLookStmt {
    private final StringExpr thought;
    private final NumExpr secs;
    private final ImmutableList<ASTNode> children;

    public ThinkForSecs(StringExpr thought, NumExpr secs) {
        this.thought = thought;
        this.secs = secs;
        children = ImmutableList.<ASTNode>builder().add(thought).add(secs).build();
    }

    public StringExpr getThought() {
        return thought;
    }

    public NumExpr getSecs() {
        return secs;
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