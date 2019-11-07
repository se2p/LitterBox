package scratch.newast.model.statement.spritelook;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;
import scratch.newast.model.costume.Costume;

public class SwitchCostumeTo implements SpriteLookStmt {
    private final Costume costume;
    private final ImmutableList<ASTNode> children;

    public SwitchCostumeTo(Costume costume) {
        this.costume = costume;
        children = ImmutableList.<ASTNode>builder().add(costume).build();
    }

    public Costume getCostume() {
        return costume;
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