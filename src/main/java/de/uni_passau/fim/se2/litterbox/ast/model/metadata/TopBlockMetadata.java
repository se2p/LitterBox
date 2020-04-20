package de.uni_passau.fim.se2.litterbox.ast.model.metadata;

import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public class TopBlockMetadata extends BlockMetadata {
    private int xPos;
    private int yPos;

    public TopBlockMetadata(CommentMetadata commentMetadata, String blockId, String opcode, String next,
                            String parent, InputMetadataList inputMetadata, InputMetadataList fields,
                            boolean topLevel, boolean shadow, int xPos, int yPos) {
        super(commentMetadata, blockId, opcode, next, parent, inputMetadata, fields, topLevel, shadow);
        this.xPos = xPos;
        this.yPos = yPos;
    }

    public int getxPos() {
        return xPos;
    }

    public int getyPos() {
        return yPos;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }
}
