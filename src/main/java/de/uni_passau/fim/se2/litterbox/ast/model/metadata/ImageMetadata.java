package de.uni_passau.fim.se2.litterbox.ast.model.metadata;

import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public class ImageMetadata extends RessourceMetadata {

    private int bitmapResolution;
    private double rotationCenterX;
    private double rotationCenterY;

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }
}
