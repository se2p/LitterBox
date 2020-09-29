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
package de.uni_passau.fim.se2.litterbox.ast.model.timecomp;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTLeaf;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

import java.util.Objects;

public class TimeComp extends AbstractNode implements ASTLeaf {

    public enum TimeCompType {
        DATE("date"),
        DAY_OF_WEEK("dayofweek"),
        HOUR("hour"),
        MINUTE("minute"),
        MONTH("month"),
        SECOND("second"),
        YEAR("year");

        private final String label;

        TimeCompType(String label) {
            this.label = label;
        }

        public static TimeCompType fromString(String text) {
            for (TimeCompType t : values()) {
                if (t.getLabel().equals(text)) {
                    return t;
                }
            }
            throw new IllegalArgumentException("Unknown type of time component: " + text);
        }

        public String getLabel() {
            return label;
        }
    }

    private TimeCompType type;

    public TimeComp(String typeName) {
        this.type = TimeCompType.fromString(typeName);
    }

    public TimeCompType getType() {
        return type;
    }

    public String getTypeName() {
        return type.getLabel();
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
        return getClass().getSimpleName();
    }

    @Override
    public BlockMetadata getMetadata() {
        return new NoBlockMetadata();
    }

    @Override
    public String[] toSimpleStringArray() {
        String[] result = new String[1];
        result[0] = type.getLabel();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TimeComp)) return false;
        TimeComp timeComp = (TimeComp) o;
        return type == timeComp.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }
}
