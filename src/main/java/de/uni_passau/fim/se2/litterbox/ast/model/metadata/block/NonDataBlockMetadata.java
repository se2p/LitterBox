package de.uni_passau.fim.se2.litterbox.ast.model.metadata.block;

import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astLists.FieldsMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astLists.InputMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public class NonDataBlockMetadata extends AbstractNode implements BlockMetadata {
    private String commentId;
    private String blockId;
    private String opcode;
    private String next;
    private String parent;
    private InputMetadataList inputMetadata;
    private FieldsMetadataList fields;
    private boolean topLevel;
    private boolean shadow;
    private MutationMetadata mutation;

    public NonDataBlockMetadata(String commentId, String blockId, String opcode, String next, String parent,
                                InputMetadataList inputMetadata, FieldsMetadataList fields, boolean topLevel,
                                boolean shadow, MutationMetadata mutation) {
        super(inputMetadata, fields, mutation);
        this.commentId = commentId;
        this.blockId = blockId;
        this.opcode = opcode;
        this.next = next;
        this.parent = parent;
        this.inputMetadata = inputMetadata;
        this.fields = fields;
        this.topLevel = topLevel;
        this.shadow = shadow;
        this.mutation = mutation;
    }

    public String getCommentId() {
        return commentId;
    }

    public String getBlockId() {
        return blockId;
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

    public FieldsMetadataList getFields() {
        return fields;
    }

    public boolean isTopLevel() {
        return topLevel;
    }

    public boolean isShadow() {
        return shadow;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public MutationMetadata getMutation() {
        return mutation;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }
}
