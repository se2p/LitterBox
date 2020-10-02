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
package de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTLeaf;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

import java.util.Objects;

public class GraphicEffect extends AbstractNode implements ASTLeaf {

    public enum GraphicEffectType {
        COLOR("color"), GHOST("ghost"), BRIGHTNESS("brightness"), WHIRL("whirl"), FISHEYE("fisheye"), PIXELATE("pixelate"),
        MOSAIC("mosaic");

        private final String token;

        GraphicEffectType(String token) {
            this.token = token;
        }

        public static boolean contains(String effect) {
            for (GraphicEffectType value : GraphicEffectType.values()) {
                if (value.name().equals(effect.toUpperCase())) {
                    return true;
                }
            }
            return false;
        }

        public static GraphicEffectType fromString(String type) {
            for (GraphicEffectType f : values()) {
                if (f.getToken().equals(type.toLowerCase())) {
                    return f;
                }
            }
            throw new IllegalArgumentException("Unknown GraphicEffect: " + type);
        }

        public String getToken() {
            return token;
        }
    }

    private GraphicEffectType type;

    public GraphicEffect(String typeName) {
        this.type = GraphicEffectType.fromString(typeName);
    }

    public GraphicEffectType getType() {
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
        if (!(o instanceof GraphicEffect)) return false;
        GraphicEffect that = (GraphicEffect) o;
        return type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }
}
