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
package de.uni_passau.fim.se2.litterbox.ast.model.metadata.actor;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astLists.*;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public class SpriteMetadata extends ActorMetadata {
    private boolean visible;
    private double x;
    private double y;
    private double size;
    private double direction;
    private boolean draggable;
    private String rotationStyle;

    public SpriteMetadata(CommentMetadataList commentsMetadata, VariableMetadataList variables,
                          ListMetadataList lists, BroadcastMetadataList broadcasts, int currentCostume,
                          ImageMetadataList costumes, SoundMetadataList sounds, double volume, int layerOrder,
                          boolean visible, double x, double y, double size, double direction, boolean draggable,
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

    public double getX() {
        return x;
    }

    public double getY() {
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

    @Override
    public ASTNode accept(CloneVisitor visitor) {
        return visitor.visit(this);
    }
}
