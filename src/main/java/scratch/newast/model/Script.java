package scratch.newast.model;

import scratch.newast.model.event.Event;
import scratch.newast.model.statement.Stmt;

import java.util.List;

public class Script {
    private Event event;
    private List<Stmt> stmtList;

    public Script(Event event, List<Stmt> stmtList) {
        this.event = event;
        this.stmtList = stmtList;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public List<Stmt> getStmtList() {
        return stmtList;
    }

    public void setStmtList(List<Stmt> stmtList) {
        this.stmtList = stmtList;
    }
}