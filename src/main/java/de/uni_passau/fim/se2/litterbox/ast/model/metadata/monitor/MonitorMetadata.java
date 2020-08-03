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
package de.uni_passau.fim.se2.litterbox.ast.model.metadata.monitor;

import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.Metadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astLists.MonitorParamMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

/**
 * This metadata the basis for all other monitors.
 */
public abstract class MonitorMetadata extends AbstractNode implements Metadata {

    private String id;
    private String mode;
    private String opcode;
    private MonitorParamMetadataList paramsMetadata;
    private String spriteName; //TODO this can be null -> have to check when creating json from metadata
    private double width;
    private double height;
    private double x;
    private double y;
    private boolean visible;

    public MonitorMetadata(String id, String mode, String opcode, MonitorParamMetadataList paramsMetadata,
                           String spriteName, double width, double height, double x, double y, boolean visible) {
        super(paramsMetadata);
        this.id = id;
        this.mode = mode;
        this.opcode = opcode;
        this.paramsMetadata = paramsMetadata;
        this.spriteName = spriteName;
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;
        this.visible = visible;
    }

    public String getId() {
        return id;
    }

    public String getMode() {
        return mode;
    }

    public String getOpcode() {
        return opcode;
    }

    public MonitorParamMetadataList getParamsMetadata() {
        return paramsMetadata;
    }

    public String getSpriteName() {
        return spriteName;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public boolean isVisible() {
        return visible;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }
}
