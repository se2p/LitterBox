package de.uni_passau.fim.se2.litterbox.ast.model.metadata;

import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public class BlockMetadata extends AbstractNode implements Metadata {
    private CommentMetadata commentMetadata;
    private String blockId;
    private String opcode;
    private String next;
    private String parent;
    private InputMetadataList inputMetadata;
    private InputMetadataList fields;
    private boolean topLevel;
    private boolean shadow;

    public BlockMetadata(CommentMetadata commentMetadata, String blockId, String opcode, String next, String parent,
                         InputMetadataList inputMetadata, InputMetadataList fields, boolean topLevel,
                         boolean shadow) {
        super(commentMetadata, inputMetadata, fields);
        this.commentMetadata = commentMetadata;
        this.blockId = blockId;
        this.opcode = opcode;
        this.next = next;
        this.parent = parent;
        this.inputMetadata = inputMetadata;
        this.fields = fields;
        this.topLevel = topLevel;
        this.shadow = shadow;
    }

    public CommentMetadata getCommentMetadata() {
        return commentMetadata;
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

    public InputMetadataList getInputMetadata() {
        return inputMetadata;
    }

    public InputMetadataList getFields() {
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
