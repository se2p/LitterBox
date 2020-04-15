package de.uni_passau.fim.se2.litterbox.ast.model.metadata;

import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public class CommentMetadata extends AbstractNode implements Metadata {

    private String blockId;
    private int x;
    private int y;
    private int width;
    private int height;
    private boolean minimized;
    private String text;

    public CommentMetadata(String blockId, int x, int y, int width, int height, boolean minimized, String text) {
        super();
        this.blockId = blockId;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.minimized = minimized;
        this.text = text;
    }

    public String getBlockId() {
        return blockId;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isMinimized() {
        return minimized;
    }

    public String getText() {
        return text;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }
}
