package scratch.newast.model.statement;

import scratch.newast.model.expression.BoolExpr;

import java.util.List;

public class IfThenStmt extends IfStmt {
    private BoolExpr boolExpr;
    private List<Stmt> thenStmts;

    public IfThenStmt(BoolExpr boolExpr, List<Stmt> thenStmts) {
        this.boolExpr = boolExpr;
        this.thenStmts = thenStmts;
    }

    public BoolExpr getBoolExpr() {
        return boolExpr;
    }

    public void setBoolExpr(BoolExpr boolExpr) {
        this.boolExpr = boolExpr;
    }

    public List<Stmt> getThenStmts() {
        return thenStmts;
    }

    public void setThenStmts(List<Stmt> thenStmts) {
        this.thenStmts = thenStmts;
    }
}