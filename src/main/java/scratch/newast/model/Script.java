package scratch.newast.model;

import scratch.newast.model.event.Event;
import scratch.newast.model.statement.Stmt;
import scratch.newast.model.statement.termination.TerminationStmt;

import java.util.List;

public class Script {
    private Event event;
    private List<Stmt> stmtList;
    private TerminationStmt terminationStmt;

    public Script(Event event, List<Stmt> stmtList) {
        this.event = event;
        this.stmtList = stmtList;
    }

    public Script(Event event, List<Stmt> stmtList, TerminationStmt terminationStmt) {
        this.event = event;
        this.stmtList = stmtList;
        this.terminationStmt = terminationStmt;
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

    public TerminationStmt getTerminationStmt() {
        return terminationStmt;
    }

    public void setTerminationStmt(TerminationStmt terminationStmt) {
        this.terminationStmt = terminationStmt;
    }

}