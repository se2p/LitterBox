package de.uni_passau.fim.se2.litterbox.ast.model.metadata.ressources;

import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public class ImageMetadata extends ResourceMetadata {
    private double bitmapResolution;
    private double rotationCenterX;
    private double rotationCenterY;

    public ImageMetadata(String assetId, String name, String md5ext, String dataFormat, double bitmapResolution,
                         double rotationCenterX, double rotationCenterY) {
        super(assetId, name, md5ext, dataFormat);
        this.bitmapResolution = bitmapResolution;
        this.rotationCenterX = rotationCenterX;
        this.rotationCenterY = rotationCenterY;
    }

    public double getBitmapResolution() {
        return bitmapResolution;
    }

    public double getRotationCenterX() {
        return rotationCenterX;
    }

    public double getRotationCenterY() {
        return rotationCenterY;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }
}
