package scratch.newast.model.event;

import scratch.newast.model.Message;

public class ReceptionOfMessage extends Event {
    private Message msg;

    public ReceptionOfMessage(Message msg) {
        this.msg = msg;
    }

    public Message getMsg() {
        return msg;
    }

    public void setMsg(Message msg) {
        this.msg = msg;
    }
}