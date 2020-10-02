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
package de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTLeaf;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

import java.util.Objects;

public class RotationStyle extends AbstractNode implements ASTLeaf {

    public enum RotationStyleType {
        dont_rotate("don't rotate"),
        left_right("left-right"),
        all_around("all around");

        private final String token;

        RotationStyleType(String token) {
            this.token = token;
        }

        public static RotationStyleType fromString(String type) {
            for (RotationStyleType f : values()) {
                if (f.getToken().equals(type.toLowerCase())) {
                    return f;
                }
            }
            throw new IllegalArgumentException("Unknown RotationStyle: " + type);
        }

        public static boolean contains(String opcode) {
            for (RotationStyleType value : RotationStyleType.values()) {
                if (value.toString().equals(opcode)) {
                    return true;
                }
            }
            return false;
        }

        public String getToken() {
            return token;
        }
    }

    private RotationStyleType type;

    public RotationStyle(String typeName) {
        this.type = RotationStyleType.fromString(typeName);
    }

    public RotationStyleType getType() {
        return type;
    }

    public String getTypeName() {
        return type.getToken();
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public ASTNode accept(CloneVisitor visitor) {
        return visitor.visit(this);
    }

    @Override
    public String getUniqueName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String[] toSimpleStringArray() {
        String[] result = new String[1];
        result[0] = type.getToken();
        return result;
    }

    @Override
    public String toString() {
        return type.getToken();
    }

    @Override
    public BlockMetadata getMetadata() {
        return new NoBlockMetadata();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RotationStyle)) return false;
        RotationStyle that = (RotationStyle) o;
        return type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }
}
