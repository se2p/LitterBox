/*
 * Copyright (C) 2019-2022 LitterBox contributors
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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ColorLiteralTest {
    @Test
    void formatRGBWhite() {
        final ColorLiteral color = new ColorLiteral(255, 255, 255);
        assertEquals("#ffffff", color.getRGB());
    }

    @Test
    void formatRGBBlack() {
        final ColorLiteral color = new ColorLiteral(0, 0, 0);
        assertEquals("#000000", color.getRGB());
    }

    @Test
    void formatRGB() {
        final ColorLiteral color = new ColorLiteral(243, 146, 0);
        assertEquals("#f39200", color.getRGB());
    }
}
