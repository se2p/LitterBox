package scratch.newast.model.statement.spritelook;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;
import scratch.newast.model.expression.num.NumExpr;
import scratch.newast.model.expression.string.StringExpr;

public class SayForSecs implements SpriteLookStmt {

    private final StringExpr string;
    private final NumExpr secs;
    private final ImmutableList<ASTNode> children;

    public SayForSecs(StringExpr string, NumExpr secs) {
        this.string = string;
        this.secs = secs;
        children = ImmutableList.<ASTNode>builder().add(string).add(secs).build();
    }

    public StringExpr getString() {
        return string;
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

