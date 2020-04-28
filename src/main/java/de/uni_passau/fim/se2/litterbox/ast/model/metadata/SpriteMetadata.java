package de.uni_passau.fim.se2.litterbox.ast.model.metadata;

import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astLists.*;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public class SpriteMetadata extends ActorMetadata {
    private boolean visible;
    private int x;
    private int y;
    private double size;
    private double direction;
    private boolean draggable;
    private String rotationStyle;

    public SpriteMetadata(CommentMetadataList commentsMetadata, VariableMetadataList variables,
                          ListMetadataList lists, BroadcastMetadataList broadcasts, int currentCostume,
                          ImageMetadataList costumes, SoundMetadataList sounds, int volume, int layerOrder,
                          boolean visible, int x, int y, double size, double direction, boolean draggable,
                          String rotationStyle) {
        super(commentsMetadata, variables, lists, broadcasts, currentCostume, costumes, sounds, volume, layerOrder);
        this.visible = visible;
        this.x = x;
        this.y = y;
        this.size = size;
        this.direction = direction;
        this.draggable = draggable;
        this.rotationStyle = rotationStyle;
    }

    public boolean isVisible() {
        return visible;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public double getSize() {
        return size;
    }

    public double getDirection() {
        return direction;
    }

    public boolean isDraggable() {
        return draggable;
    }

    public String getRotationStyle() {
        return rotationStyle;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }
}
