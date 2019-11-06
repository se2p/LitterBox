package scratch.newast.model.expression.num;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;
import scratch.newast.model.timecomp.TimeComp;

public class Current implements NumExpr {
    private final TimeComp timeComp;
    private final ImmutableList<ASTNode> children;

    public Current(TimeComp timeComp) {
        this.timeComp = timeComp;
        children = ImmutableList.<ASTNode>builder().add(timeComp).build();
    }

    public TimeComp getTimeComp() {
        return timeComp;
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