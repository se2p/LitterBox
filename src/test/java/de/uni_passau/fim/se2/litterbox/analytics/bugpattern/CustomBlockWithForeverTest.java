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
import de.uni_passau.fim.se2.litterbox.analytics.IssueReport;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParser;
import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class CustomBlockWithForeverTest {
    private static Program empty;
    private static Program procedureWithNoForever;
    private static Program procedureWithForever;
    private static Program lastCall;
    private static ObjectMapper mapper = new ObjectMapper();

    @BeforeAll
    public static void setUp() throws IOException, ParsingException {

        File f = new File("./src/test/fixtures/emptyProject.json");
        empty = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/bugpattern/procedureWithNoForever.json");
        procedureWithNoForever = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/bugpattern/procedureWithForever.json");
        procedureWithForever = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/bugpattern/callLast.json");
        lastCall = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
    }

    @Test
    public void testEmptyProgram() {
        CustomBlockWithForever parameterName = new CustomBlockWithForever();
        Set<Issue> reports = parameterName.check(empty);
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testProcedureWithNoForever() {
        CustomBlockWithForever parameterName = new CustomBlockWithForever();
        Set<Issue> reports = parameterName.check(procedureWithNoForever);
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testProcedureWithForever() {
        CustomBlockWithForever parameterName = new CustomBlockWithForever();
        Set<Issue> reports = parameterName.check(procedureWithForever);
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void testlastCall() {
        CustomBlockWithForever parameterName = new CustomBlockWithForever();
        Set<Issue> reports = parameterName.check(lastCall);
        Assertions.assertEquals(0, reports.size());
    }
}
