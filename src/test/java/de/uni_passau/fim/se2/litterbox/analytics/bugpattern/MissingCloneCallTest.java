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
package de.uni_passau.fim.se2.litterbox.analytics.bugpattern;

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

public class MissingCloneCallTest {
    private static Program empty;
    private static Program missingCloneCall;
    private static Program cloneInOtherSprite;
    private static Program rainbowSix;
    private static Program jumper;
    private static Program emptyCloneCall;
    private static ObjectMapper mapper = new ObjectMapper();

    @BeforeAll
    public static void setUp() throws IOException, ParsingException {

        File f = new File("./src/test/fixtures/emptyProject.json");
        empty = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/bugpattern/missingCloneCall.json");
        missingCloneCall = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/bugpattern/cloneInOtherSprite.json");
        cloneInOtherSprite = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/bugpattern/rainbowSix.json");
        rainbowSix = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/bugpattern/jumper.json");
        jumper = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/bugpattern/emptyCloneCall.json");
        emptyCloneCall = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
    }

    @Test
    public void testEmptyProgram() {
        MissingCloneCall parameterName = new MissingCloneCall();
        IssueReport report = parameterName.check(empty);
        Assertions.assertEquals(0, report.getCount());
    }

    @Test
    public void testClone() {
        MissingCloneCall parameterName = new MissingCloneCall();
        IssueReport report = parameterName.check(emptyCloneCall);
        Assertions.assertEquals(0, report.getCount());
    }

    @Test
    public void testMissingCloneCall() {
        MissingCloneCall parameterName = new MissingCloneCall();
        IssueReport report = parameterName.check(missingCloneCall);
        Assertions.assertEquals(1, report.getCount());
    }

    @Test
    public void testCloneInOtherSprite() {
        MissingCloneCall parameterName = new MissingCloneCall();
        IssueReport report = parameterName.check(cloneInOtherSprite);
        Assertions.assertEquals(0, report.getCount());
    }

    @Test
    public void testRainbowSix() {
        MissingCloneCall parameterName = new MissingCloneCall();
        IssueReport report = parameterName.check(rainbowSix);
        Assertions.assertEquals(0, report.getCount());
    }

    @Test
    public void testJumper() {
        MissingCloneCall parameterName = new MissingCloneCall();
        IssueReport report = parameterName.check(jumper);
        Assertions.assertEquals(1, report.getCount());
    }
}