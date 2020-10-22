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
package de.uni_passau.fim.se2.litterbox.analytics.bugpattern;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

public class ExpressionAsTouchingOrColorTest {
    private static Program empty;
    private static Program expressionColor;
    private static Program giant;
    private static Program two;

    @BeforeAll
    public static void setUp() throws IOException, ParsingException {

        empty = JsonTest.parseProgram("./src/test/fixtures/emptyProject.json");
        expressionColor = JsonTest.parseProgram("./src/test/fixtures/bugpattern/touchingExpressions.json");
        giant = JsonTest.parseProgram("./src/test/fixtures/bugpattern/exprLit.json");
        two = JsonTest.parseProgram("./src/test/fixtures/bugpattern/twoNotColo.json");
    }

    @Test
    public void testEmptyProgram() {
        ExpressionAsTouchingOrColor parameterName = new ExpressionAsTouchingOrColor();
        Set<Issue> reports = parameterName.check(empty);
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testExpressionAsColor() {
        ExpressionAsTouchingOrColor parameterName = new ExpressionAsTouchingOrColor();
        Set<Issue> reports = parameterName.check(expressionColor);
        Assertions.assertEquals(3, reports.size());
    }

    @Test
    public void testGiant() {
        ExpressionAsTouchingOrColor parameterName = new ExpressionAsTouchingOrColor();
        Set<Issue> reports = parameterName.check(giant);
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testTwo() {
        ExpressionAsTouchingOrColor parameterName = new ExpressionAsTouchingOrColor();
        Set<Issue> reports = parameterName.check(two);
        Assertions.assertEquals(2, reports.size());
    }
}
