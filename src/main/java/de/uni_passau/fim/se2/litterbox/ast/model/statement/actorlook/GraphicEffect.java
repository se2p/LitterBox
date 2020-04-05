/*
 * Copyright (C) 2019 LitterBox contributors
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
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

import java.util.Collections;
import java.util.List;

public enum GraphicEffect implements ASTLeaf {

    COLOR("COLOR"), GHOST("GHOST"), BRIGHTNESS("BRIGHTNESS"), WHIRL("WHIRL"), FISHEYE("FISHEYE"), PIXELATE("PIXELATE"),
    MOSAIC("MOSAIC");

    private final String token;

    GraphicEffect(String token) {
        this.token = token;
    }

    public static boolean contains(String effect) {
        for (GraphicEffect value : GraphicEffect.values()) {
            if (value.name().equals(effect.toUpperCase())) {
                return true;
            }
        }
        return false;
    }

    public String getToken() {
        return token;
    }

    public static GraphicEffect fromString(String type) {
        for (GraphicEffect f : values()) {
            if (f.getToken().equals(type.toUpperCase())) {
                return f;
            }
        }
        throw new IllegalArgumentException("Unknown GraphicEffect: " + type);
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
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
}
