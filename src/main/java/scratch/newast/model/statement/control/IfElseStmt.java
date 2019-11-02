package scratch.newast.model.statement.control;

import scratch.newast.model.expression.bool.BoolExpr;
import scratch.newast.model.statement.Stmt;

import java.util.List;

public class IfElseStmt implements IfStmt {
    private BoolExpr boolExpr;
    private List<Stmt> elseStmts;

    public IfElseStmt(BoolExpr boolExpr, List<Stmt> elseStmts) {
        super();
        this.boolExpr = boolExpr;
        this.elseStmts = elseStmts;
    }

    public BoolExpr getBoolExpr() {
        return boolExpr;
    }

    public void setBoolExpr(BoolExpr boolExpr) {
        this.boolExpr = boolExpr;
    }

    public List<Stmt> getElseStmts() {
        return elseStmts;
    }

    public void setElseStmts(List<Stmt> elseStmts) {
        this.elseStmts = elseStmts;
    }
}