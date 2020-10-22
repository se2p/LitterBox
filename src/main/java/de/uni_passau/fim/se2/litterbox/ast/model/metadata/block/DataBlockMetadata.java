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
package de.uni_passau.fim.se2.litterbox.ast.model.metadata.block;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTLeaf;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public class DataBlockMetadata extends AbstractNode implements BlockMetadata, ASTLeaf {
    private String blockId;
    private int dataType;
    private String dataName;
    private String dataReference;
    private double x;
    private double y;

    public DataBlockMetadata(String blockId, int dataType, String dataName, String dataReference, double x, double y) {
        super();
        this.blockId = blockId;
        this.dataType = dataType;
        this.dataName = dataName;
        this.dataReference = dataReference;
        this.x = x;
        this.y = y;
    }

    public int getDataType() {
        return dataType;
    }

    public String getDataName() {
        return dataName;
    }

    public String getDataReference() {
        return dataReference;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public String getBlockId() {
        return blockId;
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
