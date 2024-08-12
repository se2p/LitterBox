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
package de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.option;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.MBlockVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.Objects;

public class RobotDirection extends AbstractNode implements MBlockOption {

    private final EventDirectionType directionType;

    public RobotDirection(String directionName) {
        this.directionType = EventDirectionType.fromString(directionName);
    }

    public EventDirectionType getDirectionType() {
        return directionType;
    }

    public String getDirectionName() {
        return directionType.getName();
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void accept(MBlockVisitor visitor) {
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
    public BlockMetadata getMetadata() {
        return new NoBlockMetadata();
    }

    @Override
    public String[] toSimpleStringArray() {
        String[] result = new String[1];
        result[0] = directionType.getName();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RobotDirection that)) return false;
        return directionType == that.directionType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(directionType);
    }

    public enum EventDirectionType {
        LEFT("left"),
        RIGHT("right"),
        BACKWARD("backward"),
        FORWARD("forward");
        private final String name;

        EventDirectionType(String name) {
            this.name = Preconditions.checkNotNull(name);
        }

        public static EventDirectionType fromString(String name) {
            name = switch (name) {
                case "1" -> "forward";
                case "2" -> "backward";
                case "turn_left", "3" -> "left";
                case "turn_right", "4" -> "right";
                default -> name;
            };

            for (EventDirectionType f : values()) {
                if (f.getName().equals(name.toLowerCase())) {
                    return f;
                }
            }
            throw new IllegalArgumentException("Unknown EventDirection: " + name);
        }

        public String getName() {
            return name;
        }
    }
}

