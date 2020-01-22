/*
 * Copyright (C) 2019 LitterBox contributors
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
package analytics.bugpattern;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import analytics.IssueReport;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import scratch.ast.ParsingException;
import scratch.ast.model.Program;
import scratch.ast.parser.ProgramParser;

public class ProcedureWithForeverTest {
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
        ProcedureWithForever parameterName = new ProcedureWithForever();
        IssueReport report = parameterName.check(empty);
        Assertions.assertEquals(0, report.getCount());
    }

    @Test
    public void testProcedureWithNoForever() {
        ProcedureWithForever parameterName = new ProcedureWithForever();
        IssueReport report = parameterName.check(procedureWithNoForever);
        Assertions.assertEquals(0, report.getCount());
    }

    @Test
    public void testProcedureWithForever() {
        ProcedureWithForever parameterName = new ProcedureWithForever();
        IssueReport report = parameterName.check(procedureWithForever);
        Assertions.assertEquals(1, report.getCount());
    }

    @Test
    public void testlastCall() {
        ProcedureWithForever parameterName = new ProcedureWithForever();
        IssueReport report = parameterName.check(lastCall);
        Assertions.assertEquals(0, report.getCount());
    }
}
