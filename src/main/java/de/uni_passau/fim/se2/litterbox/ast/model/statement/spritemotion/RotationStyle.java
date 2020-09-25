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
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

import java.util.Collections;
import java.util.List;

public enum RotationStyle implements ASTLeaf {

    dont_rotate("don't rotate"),
    left_right("left-right"),
    all_around("all around");

    private final String token;

    RotationStyle(String token) {
        this.token = token;
    }

    public static RotationStyle fromString(String type) {
        for (RotationStyle f : values()) {
            if (f.getToken().equals(type.toLowerCase())) {
                return f;
            }
        }
        throw new IllegalArgumentException("Unknown RotationStyle: " + type);
    }

    public static boolean contains(String opcode) {
        for (RotationStyle value : RotationStyle.values()) {
            if (value.toString().equals(opcode)) {
                return true;
            }
        }
        return false;
    }

    public String getToken() {
        return token;
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
    public List<? extends ASTNode> getChildren() {
        return Collections.emptyList();
    }

    @Override
    public String getUniqueName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String[] toSimpleStringArray() {
        String[] result = new String[1];
        result[0] = token;
        return result;
    }

    @Override
    public String toString() {
        return token;
    }

    @Override
    public BlockMetadata getMetadata() {
        return new NoBlockMetadata();
    }
}
