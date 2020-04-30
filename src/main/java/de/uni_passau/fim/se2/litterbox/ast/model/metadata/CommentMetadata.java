package de.uni_passau.fim.se2.litterbox.ast.model.metadata;

import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public class CommentMetadata extends AbstractNode implements Metadata {

    private String commentId;
    private String blockId;
    private double x;
    private double y;
    private int width;
    private int height;
    private boolean minimized;
    private String text;

    public CommentMetadata(String commentId, String blockId, double x, double y, int width, int height,
                           boolean minimized,
                           String text) {
        super();
        this.commentId = commentId;
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

    public double getX() {
        return x;
    }

    public double getY() {
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

    public String getCommentId() {
        return commentId;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }
}
