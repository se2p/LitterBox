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
import analytics.IssueReport;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ast.ParsingException;
import ast.model.Program;
import ast.parser.ProgramParser;

import java.io.File;
import java.io.IOException;

public class AmbiguousParameterNameTest {
    private static Program empty;
    private static Program ambiguousParams;
    private static Program clans;
    private static Program realAmbiguousParam;
    private static ObjectMapper mapper = new ObjectMapper();

    @BeforeAll
    public static void setUp() throws IOException, ParsingException {

        File f = new File("./src/test/fixtures/emptyProject.json");
        empty = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/bugpattern/ambiguousParameters.json");
        ambiguousParams = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/bugpattern/clans.json");
        clans = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/bugpattern/realAmbiguousParameter.json");
        realAmbiguousParam = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
    }

    @Test
    public void testEmptyProgram() {
        AmbiguousParameterName parameterName = new AmbiguousParameterName();
        IssueReport report = parameterName.check(empty);
        Assertions.assertEquals(0,report.getCount() );
    }

    @Test
    public void testAmbiguousParameters(){
        AmbiguousParameterName parameterName = new AmbiguousParameterName();
        IssueReport report = parameterName.check(ambiguousParams);
        Assertions.assertEquals(0, report.getCount());
    }

    @Test
    public void testClans() {
        AmbiguousParameterName parameterName = new AmbiguousParameterName();
        IssueReport report = parameterName.check(clans);
        Assertions.assertEquals(0, report.getCount());
    }

    @Test
    public void testReal() {
        AmbiguousParameterName parameterName = new AmbiguousParameterName();
        IssueReport report = parameterName.check(realAmbiguousParam);
        Assertions.assertEquals(1, report.getCount());
    }
}
