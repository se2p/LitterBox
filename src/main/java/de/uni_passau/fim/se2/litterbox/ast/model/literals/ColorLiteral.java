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
package de.uni_passau.fim.se2.litterbox.ast.model.literals;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTLeaf;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.color.Color;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.Objects;

public class ColorLiteral extends AbstractNode implements Color, ASTLeaf {

    private final long red;
    private final long green;
    private final long blue;

    public ColorLiteral(long red, long green, long blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public long getRed() {
        return red;
    }

    public long getBlue() {
        return blue;
    }

    public long getGreen() {
        return green;
    }

    /**
     * Parses an RGB color given in hexadecimal format.
     *
     * <p>Example input: {@code #12ff7c}.
     *
     * @param hex An RGB color in hexadecimal format.
     * @return The parsed color.
     * @throws IllegalArgumentException In case the color does not have the required format.
     */
    public static ColorLiteral tryFromRgbHexString(final String hex) throws IllegalArgumentException {
        Preconditions.checkArgument(
                hex.startsWith("#"), "Color in hexadecimal form should have the format '#xxxxxx'."
        );
        Preconditions.checkArgument(
                hex.length() == 7, "Color in hexadecimal form should have the format '#xxxxxx'."
        );

        try {
            final long r = Long.parseLong(hex.substring(1, 3), 16);
            final long g = Long.parseLong(hex.substring(3, 5), 16);
            final long b = Long.parseLong(hex.substring(5, 7), 16);

            return new ColorLiteral(r, g, b);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid hexadecimal form.", e);
        }
    }

    /**
     * Formats the colour in the canonical six digit hexadecimal RGB format.
     *
     * <p>Example: red=255, green=128, blue=0 will be formatted as {@code #ff8000}.
     *
     * @return The colour.
     */
    public String getRGB() {
        return String.format("#%02x%02x%02x", red, green, blue);
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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ColorLiteral that = (ColorLiteral) o;
        return red == that.red
                && green == that.green
                && blue == that.blue;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), red, green, blue);
    }
}
