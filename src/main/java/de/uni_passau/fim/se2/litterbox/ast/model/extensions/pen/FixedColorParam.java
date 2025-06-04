/*
 * Copyright (C) 2019-2024 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.ast.model.extensions.pen;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.FixedNodeOption;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.PenExtensionVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

public class FixedColorParam extends AbstractNode implements ColorParam, FixedNodeOption {
    private final BlockMetadata metadata;
    private final FixedColorParamType type;

    public FixedColorParam(String typeName, BlockMetadata metadata) {
        super(metadata);
        this.type = FixedColorParamType.fromString(typeName);
        this.metadata = metadata;
    }

    public FixedColorParamType getType() {
        return type;
    }

    @Override
    public BlockMetadata getMetadata() {
        return metadata;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        if (visitor instanceof PenExtensionVisitor penExtensionVisitor) {
            penExtensionVisitor.visit(this);
        } else {
            visitor.visit(this);
        }
    }

    @Override
    public void accept(PenExtensionVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public ASTNode accept(CloneVisitor visitor) {
        return visitor.visit(this);
    }

    @Override
    public String getTypeName() {
        return type.getType();
    }

    public enum FixedColorParamType {
        COLOR("color"), SATURATION("saturation"), BRIGHTNESS("brightness"), TRANSPARENCY("transparency");

        private final String type;

        FixedColorParamType(String type) {
            this.type = Preconditions.checkNotNull(type);
        }

        public static FixedColorParamType fromString(String type) {
            for (FixedColorParam.FixedColorParamType f : values()) {
                if (f.getType().equals(type.toLowerCase())) {
                    return f;
                }
            }
            throw new IllegalArgumentException("Unknown FixedColorParam: " + type);
        }

        public String getType() {
            return type;
        }
    }
}
