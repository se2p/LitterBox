package scratch.newast.model.statement;

import scratch.newast.model.expression.BoolExpr;

import java.util.List;

public class UntilStmt implements ControlStmt {
    private BoolExpr boolExpr;
    private List<Stmt> stmtList;

    public UntilStmt(BoolExpr boolExpr, List<Stmt> stmtList) {
        this.boolExpr = boolExpr;
        this.stmtList = stmtList;
    }

    public BoolExpr getBoolExpr() {
        return boolExpr;
    }

    public void setBoolExpr(BoolExpr boolExpr) {
        this.boolExpr = boolExpr;
    }

    public List<Stmt> getStmtList() {
        return stmtList;
    }

    public void setStmtList(List<Stmt> stmtList) {
        this.stmtList = stmtList;
    }
}