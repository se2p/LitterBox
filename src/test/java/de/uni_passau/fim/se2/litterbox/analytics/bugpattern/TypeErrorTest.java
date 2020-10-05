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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

public class TypeErrorTest implements JsonTest {
    private static Program empty;
    private static Program stringNumber;
    private static Program numberString;
    private static Program loudnessNumber;
    private static Program complex;
    private static Program motivation;
    private static Program booleanEquals;

    @BeforeAll
    public static void setUp() throws IOException, ParsingException {

        empty = JsonTest.parseProgram("./src/test/fixtures/emptyProject.json");
        stringNumber = JsonTest.parseProgram("./src/test/fixtures/bugpattern/stringComparedToNumber.json");
        complex = JsonTest.parseProgram("./src/test/fixtures/bugpattern/complexComparison.json");
        loudnessNumber = JsonTest.parseProgram("./src/test/fixtures/bugpattern/compareLoudnessToNumber.json");

        motivation = JsonTest.parseProgram("./src/test/fixtures/bugpattern/motivation.json");
        numberString = JsonTest.parseProgram("./src/test/fixtures/bugpattern/numberComparedToString.json");
        booleanEquals = JsonTest.parseProgram("./src/test/fixtures/bugpattern/redundantBooleanEquals.json");
    }

    @Test
    public void testEmptyProgram() {
        TypeError parameterName = new TypeError();
        Set<Issue> issues = parameterName.check(empty);
        Assertions.assertEquals(0, issues.size());
    }

    @Test
    public void testStringComparedToNumber() {
        TypeError parameterName = new TypeError();
        Set<Issue> issues = parameterName.check(stringNumber);
        Assertions.assertEquals(1, issues.size());
    }

    @Test
    public void testNumberComparedToString() {
        TypeError parameterName = new TypeError();
        Set<Issue> issues = parameterName.check(numberString);
        Assertions.assertEquals(1, issues.size());
    }

    @Test
    public void testLoudnessComparedToNumber() {
        TypeError parameterName = new TypeError();
        Set<Issue> issues = parameterName.check(loudnessNumber);
        Assertions.assertEquals(0, issues.size());
    }

    @Test
    public void testComplexComparison() {
        TypeError parameterName = new TypeError();
        Set<Issue> issues = parameterName.check(complex);
        Assertions.assertEquals(2, issues.size());
    }

    @Test
    public void testMotivation() {
        TypeError parameterName = new TypeError();
        Set<Issue> issues = parameterName.check(motivation);
        Assertions.assertEquals(1, issues.size());
    }

    @Test
    public void testRedundantBooleanEquals() {
        TypeError parameterName = new TypeError();
        Set<Issue> issues = parameterName.check(booleanEquals);
        Assertions.assertEquals(1, issues.size());
    }

}
