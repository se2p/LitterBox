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

public class PositionEqualsCheckStrictTest {
    private static Program empty;
    private static Program equalXStrict;
    private static final ObjectMapper mapper = new ObjectMapper();
    private static Program equalstrict;
    private static Program deadEquals;

    @BeforeAll
    public static void setUp() throws IOException, ParsingException {

        File f = new File("./src/test/fixtures/emptyProject.json");
        empty = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));

        f = new File("./src/test/fixtures/bugpattern/positionEqualsCheckStrict.json");
        equalXStrict = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/bugpattern/positionEqualsCheckStrict2.json");
        equalstrict = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/bugpattern/deadPositionEquals.json");
        deadEquals = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
    }

    @Test
    public void testEmptyProgram() {
        PositionEqualsCheck parameterName = new PositionEqualsCheck();
        parameterName.setIgnoreLooseBlocks(true);
        Set<Issue> reports = parameterName.check(empty);
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testEqualCond() {
        PositionEqualsCheck parameterName = new PositionEqualsCheck();
        parameterName.setIgnoreLooseBlocks(true);
        Set<Issue> reports = parameterName.check(equalXStrict);
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void testEqualDistCond() {
        PositionEqualsCheck parameterName = new PositionEqualsCheck();
        parameterName.setIgnoreLooseBlocks(true);
        Set<Issue> reports = parameterName.check(equalstrict);
        Assertions.assertEquals(2, reports.size());
    }

    @Test
    public void testDeadEquals() {
        PositionEqualsCheck parameterName = new PositionEqualsCheck();
        parameterName.setIgnoreLooseBlocks(true);
        Set<Issue> reports = parameterName.check(deadEquals);
        Assertions.assertEquals(1, reports.size());
    }
}
