package scratch.newast.model.statement.actorlook;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;
import scratch.newast.model.elementchoice.ElementChoice;

public class SwitchBackdropAndWait implements ActorLookStmt {
    private final ElementChoice elementChoice;
    private final ImmutableList<ASTNode> children;

    public SwitchBackdropAndWait(ElementChoice elementChoice) {
        this.elementChoice = elementChoice;
        children = ImmutableList.<ASTNode>builder().add(elementChoice).build();
    }

    public ElementChoice getElementChoice() {
        return elementChoice;
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