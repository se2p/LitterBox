package de.uni_passau.fim.se2.litterbox.ast.model.metadata;

import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

import java.util.List;

public class BlockMetadata extends AbstractNode implements Metadata {
    private CommentsMetadata commentsMetadata;
    private String opcode;
    private String next;
    private String parent;
    private List<InputMetadata> inputMetadata;
    private List<InputMetadata> fields;
    private boolean topLevel;
    private boolean shadow;


    public CommentsMetadata getCommentsMetadata() {
        return commentsMetadata;
    }

    public String getOpcode() {
        return opcode;
    }

    public String getNext() {
        return next;
    }

    public String getParent() {
        return parent;
    }

    public List<InputMetadata> getInputMetadata() {
        return inputMetadata;
    }

    public List<InputMetadata> getFields() {
        return fields;
    }

    public boolean isTopLevel() {
        return topLevel;
    }

    public boolean isShadow() {
        return shadow;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }
}
