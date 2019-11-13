package scratch.newast.model;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.variable.Identifier;

public class Program implements ASTNode {
    private final Identifier ident;
    private final ActorDefinitionList actorDefinitionList;
    private final ImmutableList<ASTNode> children;

    public Program(Identifier ident, ActorDefinitionList actorDefinitionList) {
        this.ident = ident;
        this.actorDefinitionList = actorDefinitionList;
        children = ImmutableList.<ASTNode>builder().build();
    }

    public Identifier getIdent() {
        return ident;
    }

    public ActorDefinitionList getActorDefinitionList() {
        return actorDefinitionList;
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