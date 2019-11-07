package scratch.newast.model.event;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.Message;
import scratch.newast.model.ScratchVisitor;

public class ReceptionOfMessage implements Event {
    private final Message msg;
    private final ImmutableList<ASTNode> children;

    public ReceptionOfMessage(Message msg) {
        this.msg = msg;
        children = ImmutableList.<ASTNode>builder().add(msg).build();
    }

    public Message getMsg() {
        return msg;
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