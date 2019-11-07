package scratch.newast.model.statement.control;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;
import scratch.newast.model.StmtList;
import scratch.newast.model.expression.bool.BoolExpr;

public class IfThenStmt implements IfStmt {

    private final BoolExpr boolExpr;
    private final StmtList thenStmts;
    private final ImmutableList<ASTNode> children;

    public IfThenStmt(BoolExpr boolExpr, StmtList thenStmts) {
        this.boolExpr = boolExpr;
        this.thenStmts = thenStmts;
        children = ImmutableList.<ASTNode>builder().add(boolExpr).add(thenStmts).build();
    }

    public BoolExpr getBoolExpr() {
        return boolExpr;
    }


    public StmtList getThenStmts() {
        return thenStmts;
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