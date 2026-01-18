/*
 * Copyright (C) 2019-2026 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.export.goboscript;

import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.ColorTouchingColor;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.SpriteTouchingColor;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.ColorLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitUntil;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

public class GoboscriptColorTest {

    @Test
    public void testTouchingColor() {
        // <touching color (#ff0000)?>
        // ColorLiteral takes (r, g, b)
        SpriteTouchingColor touchingColor = new SpriteTouchingColor(
            new ColorLiteral(255, 0, 0),
            new NoBlockMetadata()
        );

        GoboscriptVisitor visitor = new GoboscriptVisitor();
        touchingColor.accept(visitor);
        String result = visitor.getOutput();

        // Expected: touching_color(0xff0000)
        assertThat(result).contains("touching_color(0xff0000)");
    }

    @Test
    public void testColorIsTouchingColor() {
        // <color (#ff0000) is touching (#00ff00)?>
        ColorTouchingColor colorTouchingColor = new ColorTouchingColor(
            new ColorLiteral(255, 0, 0),
            new ColorLiteral(0, 255, 0),
            new NoBlockMetadata()
        );

        GoboscriptVisitor visitor = new GoboscriptVisitor();
        colorTouchingColor.accept(visitor);
        String result = visitor.getOutput();

        // Expected: color_is_touching_color(0xff0000, 0x00ff00)
        assertThat(result).contains("color_is_touching_color(0xff0000, 0x00ff00)");
    }

    @Test
    public void testInsideWaitUntil() {
        List<Stmt> stmtList = new ArrayList<>();
        NoBlockMetadata meta = new NoBlockMetadata();

        // 0xFFF31E -> R=255, G=243, B=30
        // 0xFFFFFF -> R=255, G=255, B=255
        stmtList.add(new WaitUntil(
            new ColorTouchingColor(
                new ColorLiteral(255, 243, 30),
                new ColorLiteral(255, 255, 255),
                meta
            ),
            meta
        ));

        StmtList stmts = new StmtList(stmtList);
        GoboscriptVisitor visitor = new GoboscriptVisitor();
        visitor.visit(stmts);
        String result = visitor.getOutput();

        // Expected output with comma separator
        assertThat(result).contains("until color_is_touching_color(0xfff31e, 0xffffff) {}");
    }
}
