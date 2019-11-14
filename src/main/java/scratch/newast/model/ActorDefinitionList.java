package scratch.newast.model;

import com.google.common.collect.ImmutableList;
import java.util.List;

public class ActorDefinitionList implements ASTNode {

    List<ActorDefinition> actorDefinitionList;
    private final ImmutableList<ASTNode> children;

    public ActorDefinitionList(List<ActorDefinition> actorDefinitionList) {
        this.actorDefinitionList = actorDefinitionList;
        children = ImmutableList.<ASTNode>builder().addAll(actorDefinitionList).build();
    }

    public List<ActorDefinition> getActorDefinitionList() {
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
