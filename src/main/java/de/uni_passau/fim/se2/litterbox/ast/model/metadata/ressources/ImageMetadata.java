/*
 * Copyright (C) 2020 LitterBox contributors
 *
 * This file is part of LitterBox.
 *
 * LitterBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * LitterBox is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LitterBox. If not, see <http://www.gnu.org/licenses/>.
 */
package de.uni_passau.fim.se2.litterbox.ast.model.metadata.ressources;

import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public class ImageMetadata extends ResourceMetadata {
    private Double bitmapResolution;
    private double rotationCenterX;
    private double rotationCenterY;

    public ImageMetadata(String assetId, String name, String md5ext, String dataFormat, Double bitmapResolution,
                         double rotationCenterX, double rotationCenterY) {
        super(assetId, name, md5ext, dataFormat);
        this.bitmapResolution = bitmapResolution;
        this.rotationCenterX = rotationCenterX;
        this.rotationCenterY = rotationCenterY;
    }

    public Double getBitmapResolution() {
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