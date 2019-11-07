package scratch.newast.model.statement.control;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;
import scratch.newast.model.StmtList;
import scratch.newast.model.expression.bool.BoolExpr;

public class UntilStmt implements ControlStmt {
    private final BoolExpr boolExpr;
    private final StmtList stmtList;
    private final ImmutableList<ASTNode> children;

    public UntilStmt(BoolExpr boolExpr, StmtList stmtList) {
        this.boolExpr = boolExpr;
        this.stmtList = stmtList;
        children = ImmutableList.<ASTNode>builder().add(boolExpr).add(stmtList).build();
    }

    public BoolExpr getBoolExpr() {
        return boolExpr;
    }


    public StmtList getStmtList() {
        return stmtList;
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