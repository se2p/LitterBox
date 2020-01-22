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
package de.uni_passau.fim.se2.litterbox.analytics.ctscore;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import de.uni_passau.fim.se2.litterbox.analytics.IssueReport;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParser;

public class FlowControlTest {

    private static ObjectMapper mapper = new ObjectMapper();
    private static Program programMiddleNested;
    private static Program programTopNested;
    private static Program programTopNestedElse;
    private static Program programMiddle;
    private static Program programZero;
    private static Program empty;

    @BeforeAll
    public static void setUp() throws IOException, ParsingException {
        File f = new File("./src/test/fixtures/flowControl/flowControlNestedMiddleScore.json");
        programMiddleNested = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/flowControl/flowControlTopNestedInsideMiddle.json");
        programTopNested = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/emptyProject.json");
        empty =ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/flowControl/flowControlMiddle.json");
        programMiddle =ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/flowControl/flowControlTopNestedElse.json");
        programTopNestedElse =ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/flowControl/flowControlZero.json");
        programZero =ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
    }

    @Test
    public void testFlowControlTopNested(){
        IssueReport report = (new FlowControl()).check(programTopNested);
        Assertions.assertEquals(3, report.getCount());
    }

    @Test
    public void testFlowControlMiddleNested(){
        IssueReport report = (new FlowControl()).check(programMiddleNested);
        Assertions.assertEquals(2, report.getCount());
    }

    @Test
    public void testFlowControlMiddle(){
        IssueReport report = (new FlowControl()).check(programMiddle);
        Assertions.assertEquals(2, report.getCount());
    }

    @Test
    public void testFlowControlEmpty(){
        IssueReport report = (new FlowControl()).check(empty);
        Assertions.assertEquals(0, report.getCount());
    }

    @Test
    public void testFlowControlTopNestedElse(){
        IssueReport report = (new FlowControl()).check(programTopNestedElse);
        Assertions.assertEquals(3, report.getCount());
    }

    @Test
    public void testFlowControlZero(){
        IssueReport report = (new FlowControl()).check(programZero);
        Assertions.assertEquals(0, report.getCount());
    }
}
