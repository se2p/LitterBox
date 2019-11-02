package scratch.newast.model.statement.control;

import scratch.newast.model.expression.bool.BoolExpr;
import scratch.newast.model.statement.Stmt;

import java.util.List;

public class IfThenStmt implements IfStmt {
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