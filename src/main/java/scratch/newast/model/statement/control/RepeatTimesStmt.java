package scratch.newast.model.statement.control;

import scratch.newast.model.expression.num.NumExpr;
import scratch.newast.model.statement.Stmt;
import scratch.newast.model.statement.control.ControlStmt;

import java.util.List;

public class RepeatTimesStmt implements ControlStmt {
    private NumExpr times;
    private List<Stmt> stmtList;

    public RepeatTimesStmt(NumExpr times, List<Stmt> stmtList) {
        this.times = times;
        this.stmtList = stmtList;
    }

    public NumExpr getTimes() {
        return times;
    }

    public void setTimes(NumExpr times) {
        this.times = times;
    }

    public List<Stmt> getStmtList() {
        return stmtList;
    }

    public void setStmtList(List<Stmt> stmtList) {
        this.stmtList = stmtList;
    }
}