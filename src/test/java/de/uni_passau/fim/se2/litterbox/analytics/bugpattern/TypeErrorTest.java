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
package de.uni_passau.fim.se2.litterbox.analytics.bugpattern;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

public class TypeErrorTest implements JsonTest {

    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        assertThatFinderReports(0, new TypeError(), "./src/test/fixtures/emptyProject.json");
    }

    @Test
    public void testStringComparedToNumber() throws IOException, ParsingException {
        //must not find this, because this is part of ComparingLiterals
        assertThatFinderReports(0, new TypeError(), "./src/test/fixtures/bugpattern/stringComparedToNumber.json");
    }

    @Test
    public void testNumberComparedToString() throws IOException, ParsingException {
        assertThatFinderReports(1, new TypeError(), "./src/test/fixtures/bugpattern/numberComparedToString.json");
    }

    @Test
    public void testLoudnessComparedToNumber() throws IOException, ParsingException {
        assertThatFinderReports(0, new TypeError(), "./src/test/fixtures/bugpattern/compareLoudnessToNumber.json");
    }

    @Test
    public void testComplexComparison() throws IOException, ParsingException {
        assertThatFinderReports(1, new TypeError(), "./src/test/fixtures/bugpattern/complexComparison.json");
    }

    @Test
    public void testMotivation() throws IOException, ParsingException {
        assertThatFinderReports(1, new TypeError(), "./src/test/fixtures/bugpattern/motivation.json");
    }

    @Test
    public void testRedundantBooleanEquals() throws IOException, ParsingException {
        assertThatFinderReports(1, new TypeError(), "./src/test/fixtures/bugpattern/redundantBooleanEquals.json");
    }

    @Test
    public void testDistanceToWeirdCombination() throws IOException, ParsingException {
        Program booleanEquals = JsonTest.parseProgram("./src/test/fixtures/bugpattern/distanceToWeirdCombination.json");
        TypeError parameterName = new TypeError();
        Set<Issue> issues = parameterName.check(booleanEquals);
        Assertions.assertEquals(1, issues.size());
        for (Issue issue : issues) {
            Assertions.assertEquals((new Hint(TypeError.WEIRD_DISTANCE)).getHintText(), issue.getHintText());
        }
    }

    @Test
    public void testNoTypeErrorVariable() throws IOException, ParsingException {
        assertThatFinderReports(0, new TypeError(), "./src/test/fixtures/bugpattern/noTypeError.json");
    }

    @Test
    public void testComparingTouching() throws IOException, ParsingException {
        Program booleanEquals = JsonTest.parseProgram("./src/test/fixtures/bugpattern/comparingTouching.json");
        TypeError parameterName = new TypeError();
        Set<Issue> issues = parameterName.check(booleanEquals);
        Assertions.assertEquals(1, issues.size());
        for (Issue issue : issues) {
            Assertions.assertNotNull(issue.getCodeLocation());
        }
    }
}
