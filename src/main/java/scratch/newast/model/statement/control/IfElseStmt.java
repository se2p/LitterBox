package scratch.newast.model.statement.control;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;
import scratch.newast.model.StmtList;
import scratch.newast.model.expression.bool.BoolExpr;

public class IfElseStmt implements IfStmt {

    private final ImmutableList<ASTNode> children;
    private final BoolExpr boolExpr;
    private final StmtList stmtList;
    private final StmtList elseStmts;

    public IfElseStmt(BoolExpr boolExpr, StmtList stmtList, StmtList elseStmts) {
        super();
        this.boolExpr = boolExpr;
        this.stmtList = stmtList;
        this.elseStmts = elseStmts;
        ImmutableList.Builder<ASTNode> builder = ImmutableList.builder();
        children = builder.add(boolExpr).add(stmtList).add(elseStmts).build();
    }

    public BoolExpr getBoolExpr() {
        return boolExpr;
    }

    public StmtList getStmtList() {
        return stmtList;
    }

    public StmtList getElseStmts() {
        return elseStmts;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public ImmutableList<ASTNode> getChildren() {
        return children;
    }
}