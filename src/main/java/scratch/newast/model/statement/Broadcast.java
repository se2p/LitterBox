package scratch.newast.model.statement;

import scratch.newast.model.Message;

public class Broadcast extends CommonStmt {
    private Message message;

    public Broadcast(Message message) {
        this.message = message;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }
}