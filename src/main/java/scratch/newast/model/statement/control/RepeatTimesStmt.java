package scratch.newast.model.statement.control;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;
import scratch.newast.model.StmtList;
import scratch.newast.model.expression.num.NumExpr;

public class RepeatTimesStmt implements ControlStmt {
    private final NumExpr times;
    private final StmtList stmtList;
    private final ImmutableList<ASTNode> children;

    public RepeatTimesStmt(NumExpr times, StmtList stmtList) {
        this.times = times;
        this.stmtList = stmtList;
        children = ImmutableList.<ASTNode>builder().add(times).add(stmtList).build();
    }

    public NumExpr getTimes() {
        return times;
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