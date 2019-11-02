package scratch.newast.model.statement.common;

import scratch.newast.model.Message;

public class Broadcast implements CommonStmt {
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