package scratch.newast.model.statement.spritelook;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;

public class Think implements SpriteLookStmt {
    private final String thought;
    private final ImmutableList<ASTNode> children;


    public Think(String thought) {
        this.thought = thought;
        children = ImmutableList.<ASTNode>builder().build();
    }

    public String getThought() {
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