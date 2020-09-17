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
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public class TerminatedLoopTest {

    private static ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        File f = new File("./src/test/fixtures/emptyProject.json");
        Program empty = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));

        TerminatedLoop terminatedLoop = new TerminatedLoop();
        Set<Issue> reports = terminatedLoop.check(empty);
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testProcedureWithTermination() throws IOException, ParsingException {
        File f = new File("./src/test/fixtures/bugpattern/terminatedLoop.json");
        Program terminatedLoop = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));

        TerminatedLoop terminatedLoopChecker = new TerminatedLoop();
        Set<Issue> reports = terminatedLoopChecker.check(terminatedLoop);
        Assertions.assertEquals(3, reports.size());
    }

    @Test
    public void testEmptyLoopAndOneIssue() throws IOException, ParsingException {
        File f = new File("./src/test/fixtures/bugpattern/emptyLoopAndOneIssue.json");
        Program empty = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));

        TerminatedLoop terminatedLoop = new TerminatedLoop();
        Set<Issue> reports = terminatedLoop.check(empty);
        Assertions.assertEquals(1, reports.size());
    }
}

