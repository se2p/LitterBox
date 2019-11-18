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
package scratch.newAnalytics;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import newanalytics.IssueReport;
import newanalytics.smells.MissingTermination;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import scratch.newast.ParsingException;
import scratch.newast.model.Program;
import scratch.newast.parser.ProgramParser;
import org.junit.jupiter.api.Assertions;

public class MissingTerminationTest {
    private static ObjectMapper mapper = new ObjectMapper();
    private static Program program;
    private static Program programNested;

    @BeforeAll
    public static void setUp() throws IOException, ParsingException {
        File f = new File("./src/test/java/scratch/fixtures/missingTermination.json");
        program = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f= new File("./src/test/java/scratch/fixtures/missingTerminationNested.json");
        programNested = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
    }

    @Test
    public void testMissingTermination() {
        IssueReport report = (new MissingTermination()).check(program);
        Assertions.assertEquals(1, report.getCount());
    }

    @Test
    public void testMissingTerminationNested(){
        IssueReport report = (new MissingTermination()).check(programNested);
        Assertions.assertEquals(1, report.getCount());
    }
}
