package de.uni_passau.fim.se2.litterbox.ast.model.metadata.block;

import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astLists.FieldsMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astLists.InputMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public class TopNonDataBlockMetadata extends NonDataBlockMetadata {
    private double xPos;
    private double yPos;

    public TopNonDataBlockMetadata(String commentId, String blockId, String opcode, String next,
                                   String parent, InputMetadataList inputMetadata, FieldsMetadataList fields,
                                   boolean topLevel, boolean shadow, MutationMetadata mutation, double xPos,
                                   double yPos) {
        super(commentId, blockId, opcode, next, parent, inputMetadata, fields, topLevel, shadow, mutation);
        this.xPos = xPos;
        this.yPos = yPos;
    }

    public double getxPos() {
        return xPos;
    }

    public double getyPos() {
        return yPos;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }
}
