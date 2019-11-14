package scratch.newast.model;

import com.google.common.collect.ImmutableList;
import java.util.List;
import scratch.newast.model.statement.common.SetStmt;

public class SetStmtList implements ASTNode {

    private final ImmutableList<ASTNode> children;
    private List<SetStmt> setStmtList;

    public SetStmtList(List<SetStmt> setStmtList) {
        this.setStmtList = setStmtList;
        children = ImmutableList.<ASTNode>builder().build();
    }

    public List<SetStmt> getStmts() {
        return setStmtList;
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
