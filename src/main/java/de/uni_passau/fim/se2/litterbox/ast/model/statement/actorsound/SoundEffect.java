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
package de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTLeaf;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

import java.util.Objects;

public class SoundEffect extends AbstractNode implements ASTLeaf {

    public enum SoundEffectType {
        PAN("pan left/right"), PITCH("pitch");

        private final String token;

        SoundEffectType(String token) {
            this.token = token;
        }

        public static boolean contains(String opcode) {
            for (SoundEffectType value : SoundEffectType.values()) {
                if (value.name().equals(opcode)) {
                    return true;
                }
            }
            return false;
        }

        public static SoundEffectType fromString(String type) {
            for (SoundEffectType f : values()) {
                if (f.getToken().startsWith(type.toLowerCase())) {
                    return f;
                }
            }
            throw new IllegalArgumentException("Unknown SoundEffect: " + type);
        }

        public String getToken() {
            return token;
        }
    }

    private SoundEffectType type;

    public SoundEffect(String typeName) {
        this.type = SoundEffectType.fromString(typeName);
    }

    public SoundEffectType getType() {
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
    public BlockMetadata getMetadata() {
        return new NoBlockMetadata();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SoundEffect)) return false;
        SoundEffect that = (SoundEffect) o;
        return type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }
}
