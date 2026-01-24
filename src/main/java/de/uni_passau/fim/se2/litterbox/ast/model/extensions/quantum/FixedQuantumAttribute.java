/*
 * Copyright (C) 2019-2026 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.ast.model.extensions.quantum;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.FixedNodeOption;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.QuantumExtensionVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public class FixedQuantumAttribute extends AbstractNode implements FixedNodeOption, QuantumExtensionBlock {

    private final FixedQuantumAttributeType type;

    public FixedQuantumAttribute(final FixedQuantumAttributeType type) {
        this.type = type;
    }

    @Override
    public void accept(QuantumExtensionVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        if (visitor instanceof QuantumExtensionVisitor qVisitor) {
            qVisitor.visit(this);
        } else {
            visitor.visit((QuantumExtensionBlock) this);
        }
    }

    @Override
    public ASTNode accept(CloneVisitor visitor) {
        return visitor.visit(this);
    }

    public FixedQuantumAttributeType getType() {
        return type;
    }

    @Override
    public String getTypeName() {
        return type.getType();
    }

    public enum FixedQuantumAttributeType {
        POSITION("position"),
        SIZE("size"),
        DIRECTION("direction"),
        COLOR("color"),
        COSTUME("costume");

        private final String type;

        FixedQuantumAttributeType(final String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }

        public static FixedQuantumAttributeType fromString(final String type) {
            // has leading and trailing _ in the JSON
            final String typeTrimmed = type.replace("_", "");

            for (FixedQuantumAttributeType f : values()) {
                if (f.getType().equals(typeTrimmed)) {
                    return f;
                }
            }

            throw new IllegalArgumentException("Unknown FixedQuantumAttribute: " + type);
        }
    }
}
