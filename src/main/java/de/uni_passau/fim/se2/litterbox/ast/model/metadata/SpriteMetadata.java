package de.uni_passau.fim.se2.litterbox.ast.model.metadata;

import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public class SpriteMetadata extends ActorMetadata {
    private boolean visible;
    private int x;
    private int y;
    private double size;
    private double direction;
    private boolean draggable;
    private String rotationStyle;

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }
}
