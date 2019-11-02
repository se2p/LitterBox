package scratch.newast.model.statement;

import scratch.newast.model.Message;

public class BroadcastAndWait implements CommonStmt {
    private Message message;

    public BroadcastAndWait(Message message) {
        this.message = message;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }
}