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

import com.fasterxml.jackson.databind.ObjectMapper;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public class PositionEqualsCheckTest {
    private static Program empty;
    private static Program equalX;
    private static Program equalDirection;
    private static Program allChecks;
    private static Program xPositionEquals;
    private static Program nested;
    private static final ObjectMapper mapper = new ObjectMapper();
    private static Program deadEquals;

    @BeforeAll
    public static void setUp() throws IOException, ParsingException {

        File f = new File("./src/test/fixtures/emptyProject.json");
        empty = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));

        f = new File("./src/test/fixtures/bugpattern/xPosEqual.json");
        equalX = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));

        f = new File("./src/test/fixtures/bugpattern/posEqualDirection.json");
        equalDirection = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));

        f = new File("./src/test/fixtures/bugpattern/positionEqualsCheck.json");
        allChecks = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));

        f = new File("./src/test/fixtures/bugpattern/xPositionEquals.json");
        xPositionEquals = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));

        f = new File("./src/test/fixtures/bugpattern/positionEqualsNested.json");
        nested = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));

        f = new File("./src/test/fixtures/bugpattern/deadPositionEquals.json");
        deadEquals = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
    }

    @Test
    public void testEmptyProgram() {
        PositionEqualsCheck parameterName = new PositionEqualsCheck();
        Set<Issue> reports = parameterName.check(empty);
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testEqualCond() {
        PositionEqualsCheck parameterName = new PositionEqualsCheck();
        Set<Issue> reports = parameterName.check(equalX);
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void testEqualDir() {
        PositionEqualsCheck parameterName = new PositionEqualsCheck();
        Set<Issue> reports = parameterName.check(equalDirection);
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testXPositionEquals() {
        PositionEqualsCheck parameterName = new PositionEqualsCheck();
        Set<Issue> reports = parameterName.check(xPositionEquals);
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void testAll() {
        PositionEqualsCheck parameterName = new PositionEqualsCheck();
        Set<Issue> reports = parameterName.check(allChecks);
        Assertions.assertEquals(4, reports.size());
    }

    @Test
    public void testNested() {
        PositionEqualsCheck parameterName = new PositionEqualsCheck();
        Set<Issue> reports = parameterName.check(nested);
        Assertions.assertEquals(2, reports.size());
    }

    @Test
    public void testDeadEquals() {
        PositionEqualsCheck parameterName = new PositionEqualsCheck();
        Set<Issue> reports = parameterName.check(deadEquals);
        Assertions.assertEquals(2, reports.size());
    }
}
