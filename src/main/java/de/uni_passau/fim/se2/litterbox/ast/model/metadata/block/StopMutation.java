package de.uni_passau.fim.se2.litterbox.ast.model.metadata.block;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTLeaf;
import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

import java.util.List;

public class StopMutation extends AbstractNode implements MutationMetadata, ASTLeaf {
    private final String tagName;
    private final List<String> children;
    private final boolean hasNext;

    public StopMutation(String tagName, List<String> children, boolean hasNext) {
        super();
        this.tagName = tagName;
        this.children = children;
        this.hasNext = hasNext;
    }

    public String getTagName() {
        return tagName;
    }

    public List<String> getChild() {
        return children;
    }

    public boolean isHasNext() {
        return hasNext;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }
}
