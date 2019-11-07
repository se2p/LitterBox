package scratch.newast.model;

import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import scratch.newast.model.event.Event;
import scratch.structure.ast.Ast;

public class Script implements ASTNode {
    private final Event event;
    private final StmtList stmtList;
    private final ImmutableList<ASTNode> children;

    public Script(Event event, StmtList stmtList) {
        this.event = event;
        this.stmtList = stmtList;
        ImmutableList.Builder<ASTNode> builder = ImmutableList.builder();
        children = builder.add(this.event, this.stmtList).build();
    }

    public Event getEvent() {
        return event;
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