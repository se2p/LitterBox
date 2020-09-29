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
package de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTLeaf;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.Objects;

public class ForwardBackwardChoice extends AbstractNode implements ASTLeaf {

    public enum ForwardBackwardChoiceType {
        FORWARD("forward"), BACKWARD("backward");

        private final String type;

        ForwardBackwardChoiceType(String type) {
            this.type = Preconditions.checkNotNull(type);
        }

        public static ForwardBackwardChoiceType fromString(String type) {
            for (ForwardBackwardChoiceType f : values()) {
                if (f.getType().equals(type)) {
                    return f;
                }
            }
            throw new IllegalArgumentException("Unknown ForwardBackwardChoice: " + type);
        }

        public String getType() {
            return type;
        }
    }

    private ForwardBackwardChoiceType type;

    public ForwardBackwardChoice(String typeName) {
        this.type = ForwardBackwardChoiceType.fromString(typeName);
    }

    public ForwardBackwardChoiceType getType() {
        return type;
    }

    public String getTypeName() {
        return type.getType();
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
        result[0] = type.getType();
        return result;
    }

    @Override
    public BlockMetadata getMetadata() {
        return new NoBlockMetadata();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ForwardBackwardChoice)) return false;
        ForwardBackwardChoice that = (ForwardBackwardChoice) o;
        return type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }
}
