package scratch.newast.model.resource;

import com.google.common.collect.ImmutableList;
import java.util.List;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;

public class ResourceList implements ASTNode {

    List<Resource> resourceList;
    private final ImmutableList<ASTNode> children;

    public ResourceList(List<Resource> resourceList) {
        this.resourceList = resourceList;
        children = ImmutableList.<ASTNode>builder().build();
    }

    public List<Resource> getResourceList() {
        return resourceList;
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
