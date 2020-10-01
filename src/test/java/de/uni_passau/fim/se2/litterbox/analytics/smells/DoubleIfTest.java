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

package de.uni_passau.fim.se2.litterbox.analytics.smells;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

public class DoubleIfTest implements JsonTest {

    private static Program empty;
    private static Program doubleIf;
    private static Program doubleIfConditionOnVariable;
    private static Program doubleIfConditionOnDifferentVariable;
    private static Program doubleIfIfElse;
    private static Program doubleIfWithStatementBetween;
    private static Program doubleIfWithDifferentBody;

    @BeforeAll
    public static void setUp() throws IOException, ParsingException {

        empty = JsonTest.parseProgram("./src/test/fixtures/emptyProject.json");
        doubleIf = JsonTest.parseProgram("./src/test/fixtures/smells/doubleIf.json");
        doubleIfConditionOnVariable = JsonTest.parseProgram("./src/test/fixtures/smells/doubleIfCondition.json");
        doubleIfConditionOnDifferentVariable = JsonTest.parseProgram("./src/test/fixtures/smells/doubleIfConditionDifferentVariable.json");
        doubleIfIfElse = JsonTest.parseProgram("./src/test/fixtures/smells/doubleIfIfElse.json");
        doubleIfWithStatementBetween = JsonTest.parseProgram("./src/test/fixtures/smells/doubleIfWithStatementBetween.json");
        doubleIfWithDifferentBody = JsonTest.parseProgram("./src/test/fixtures/smells/doubleIfWithDifferentBody.json");
    }

    @Test
    public void testEmptyProgram() {
        DoubleIf finder = new DoubleIf();
        Set<Issue> reports = finder.check(empty);
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testProgram() {
        DoubleIf finder = new DoubleIf();
        Set<Issue> reports = finder.check(doubleIf);
        Assertions.assertEquals(2, reports.size());
    }

    @Test
    public void testDuplicateConditionOnVariable() {
        DoubleIf finder = new DoubleIf();
        Set<Issue> reports = finder.check(doubleIfConditionOnVariable);
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void testDuplicateConditionOnDifferentVariable() {
        DoubleIf finder = new DoubleIf();
        Set<Issue> reports = finder.check(doubleIfConditionOnDifferentVariable);
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testIfThenFollowedByIfElse() {
        DoubleIf finder = new DoubleIf();
        Set<Issue> reports = finder.check(doubleIfIfElse);
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void testStatementBetweenIfs() {
        DoubleIf finder = new DoubleIf();
        Set<Issue> reports = finder.check(doubleIfWithStatementBetween);
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testDoubleIfWithDifferentBody() {
        DoubleIf finder = new DoubleIf();
        // The body of the condition doesn't matter
        Set<Issue> reports = finder.check(doubleIfWithDifferentBody);
        Assertions.assertEquals(1, reports.size());
    }

}
