/*
 * Copyright (C) 2019-2021 LitterBox contributors
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
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class DoubleIfTest implements JsonTest {

    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        assertThatFinderReports(0, new DoubleIf(), "./src/test/fixtures/emptyProject.json");
    }

    @Test
    public void testProgram() throws IOException, ParsingException {
        assertThatFinderReports(1, new DoubleIf(), "./src/test/fixtures/smells/doubleIf.json");
    }

    @Test
    public void testDuplicateConditionOnVariable() throws IOException, ParsingException {
        assertThatFinderReports(1, new DoubleIf(), "./src/test/fixtures/smells/doubleIfCondition.json");
    }

    @Test
    public void testDuplicateConditionOnDifferentVariable() throws IOException, ParsingException {
        assertThatFinderReports(0, new DoubleIf(), "./src/test/fixtures/smells/doubleIfConditionDifferentVariable.json");
    }

    @Test
    public void testIfThenFollowedByIfElse() throws IOException, ParsingException {
        assertThatFinderReports(1, new DoubleIf(), "./src/test/fixtures/smells/doubleIfIfElse.json");
    }

    @Test
    public void testStatementBetweenIfs() throws IOException, ParsingException {
        assertThatFinderReports(0, new DoubleIf(), "./src/test/fixtures/smells/doubleIfWithStatementBetween.json");
    }

    @Test
    public void testDoubleIfWithDifferentBody() throws IOException, ParsingException {
        // The body of the condition doesn't matter
        assertThatFinderReports(1, new DoubleIf(), "./src/test/fixtures/smells/doubleIfWithDifferentBody.json");
    }

    @Test
    public void testNoDoubleIfMovingProgram() throws IOException, ParsingException {
        //no double if because the condition is a touching one and the first if has a moving block
        assertThatFinderReports(0, new DoubleIf(), "./src/test/fixtures/smells/noDoubleIfMoving.json");
    }
}
