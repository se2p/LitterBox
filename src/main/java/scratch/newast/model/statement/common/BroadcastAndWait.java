package scratch.newast.model.statement.common;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.Message;
import scratch.newast.model.ScratchVisitor;

public class BroadcastAndWait implements CommonStmt {
    private Message message;
    private final ImmutableList<ASTNode> children;

    public BroadcastAndWait(Message message) {
        this.message = message;
        children = ImmutableList.<ASTNode>builder().add(message).build();
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
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