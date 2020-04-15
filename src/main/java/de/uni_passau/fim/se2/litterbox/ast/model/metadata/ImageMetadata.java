package de.uni_passau.fim.se2.litterbox.ast.model.metadata;

import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public class ImageMetadata extends ResourceMetadata {
    private int bitmapResolution;
    private double rotationCenterX;
    private double rotationCenterY;

    public ImageMetadata(String assetId, String name, String md5ext, String dataFormat, int bitmapResolution,
                         double rotationCenterX, double rotationCenterY) {
        super(assetId, name, md5ext, dataFormat);
        this.bitmapResolution = bitmapResolution;
        this.rotationCenterX = rotationCenterX;
        this.rotationCenterY = rotationCenterY;
    }

    public int getBitmapResolution() {
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
