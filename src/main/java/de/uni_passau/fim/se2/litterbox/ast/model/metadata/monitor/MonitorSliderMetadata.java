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

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astLists.MonitorParamMetadataList;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

/**
 * This metadata is for all monitors that do not belong to lists.
 */
public class MonitorSliderMetadata extends MonitorMetadata {
    private double sliderMin;
    private double sliderMax;
    private boolean isDiscrete;
    private String value;

    public MonitorSliderMetadata(String id, String mode, String opcode, MonitorParamMetadataList paramsMetadata,
                                 String spriteName, double width, double height, double x, double y,
                                 boolean visible, String value, double sliderMin, double sliderMax,
                                 boolean isDiscrete) {
        super(id, mode, opcode, paramsMetadata, spriteName, width, height, x, y, visible);
        this.value = value;
        this.sliderMin = sliderMin;
        this.sliderMax = sliderMax;
        this.isDiscrete = isDiscrete;
    }

    public double getSliderMin() {
        return sliderMin;
    }

    public double getSliderMax() {
        return sliderMax;
    }

    public boolean isDiscrete() {
        return isDiscrete;
    }

    public String getValue() {
        return value;
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
