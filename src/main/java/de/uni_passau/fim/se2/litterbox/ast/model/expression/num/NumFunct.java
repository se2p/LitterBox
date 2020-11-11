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
package de.uni_passau.fim.se2.litterbox.ast.model.expression.num;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTLeaf;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.Objects;

public class NumFunct extends AbstractNode implements ASTLeaf {

    public enum NumFunctType {
        ABS("abs"),
        ACOS("acos"),
        ASIN("asin"),
        ATAN("atan"),
        CEILING("ceiling"),
        COS("cos"),
        FLOOR("floor"),
        LN("ln"),
        LOG("log"),
        POW10("10 ^"),
        POWE("e ^"),
        SIN("sin"),
        SQRT("sqrt"),
        TAN("tan"),
        UNKNOWN("?");

        private final String function;

        NumFunctType(String function) {
            this.function = Preconditions.checkNotNull(function);
        }

        public static NumFunctType fromString(String function) {
            for (NumFunctType f : values()) {
                if (f.getFunction().equals(function)) {
                    return f;
                }
            }
            throw new IllegalArgumentException("Unknown mathematical function: " + function);
        }

        public String getFunction() {
            return function;
        }
    }

    private NumFunctType type;

    public NumFunct(String type) {
        this.type = NumFunctType.fromString(type);
    }

    public NumFunctType getType() {
        return type;
    }

    public String getTypeName() {
        return type.getFunction();
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
    public BlockMetadata getMetadata() {
        return new NoBlockMetadata();
    }

    @Override
    public String[] toSimpleStringArray() {
        String[] result = new String[1];
        result[0] = type.getFunction();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NumFunct)) return false;
        NumFunct numFunct = (NumFunct) o;
        return type == numFunct.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }
}
