package de.uni_passau.fim.se2.litterbox.ast.model.metadata.block;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTLeaf;
import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

import java.util.List;

public class ExistingCallMutationMetadata extends AbstractNode implements MutationMetadata, ASTLeaf {
    private String tagName;
    private List<String> children;
    private String procCode;
    private String argumentIds;
    private boolean warp;

    public ExistingCallMutationMetadata(String tagName, List<String> children, String procCode, String argumentIds,
                                     boolean warp) {
        super();
        this.tagName = tagName;
        this.children = children;
        this.procCode = procCode;
        this.argumentIds = argumentIds;
        this.warp = warp;
    }

    public String getTagName() {
        return tagName;
    }

    public List<String> getChild() {
        return children;
    }

    public String getProcCode() {
        return procCode;
    }

    public String getArgumentIds() {
        return argumentIds;
    }

    public boolean isWarp() {
        return warp;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }
}
