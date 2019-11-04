package scratch.newast.model;

import scratch.newast.model.event.Event;

public class Script {
    private Event event;
    private StmtList stmtList;

    public Script(Event event, StmtList stmtList) {
        this.event = event;
        this.stmtList = stmtList;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public StmtList getStmtList() {
        return stmtList;
    }

    public void setStmtList(StmtList stmtList) {
        this.stmtList = stmtList;
    }
}