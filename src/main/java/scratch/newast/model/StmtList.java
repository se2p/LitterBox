package scratch.newast.model;

import scratch.newast.model.statement.spritelook.ListOfStmt;
import scratch.newast.model.statement.termination.TerminationStmt;

public class StmtList {

    ListOfStmt stmts;
    TerminationStmt terminationStmt;

    public StmtList(ListOfStmt stmts, TerminationStmt terminationStmt) {
        this.stmts = stmts;
        this.terminationStmt = terminationStmt;
    }

    public ListOfStmt getStmts() {
        return stmts;
    }

    public void setStmts(ListOfStmt stmts) {
        this.stmts = stmts;
    }

    public TerminationStmt getTerminationStmt() {
        return terminationStmt;
    }

    public void setTerminationStmt(TerminationStmt terminationStmt) {
        this.terminationStmt = terminationStmt;
    }
}
