package de.uni_passau.fim.se2.litterbox.ast.model.metadata;

import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astLists.InputMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public class TopBlockMetadata extends BlockMetadata {
    private double xPos;
    private double yPos;

    public TopBlockMetadata(CommentMetadata commentMetadata, String blockId, String opcode, String next,
                            String parent, InputMetadataList inputMetadata, InputMetadataList fields,
                            boolean topLevel, boolean shadow, double xPos, double yPos) {
        super(commentMetadata, blockId, opcode, next, parent, inputMetadata, fields, topLevel, shadow);
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
