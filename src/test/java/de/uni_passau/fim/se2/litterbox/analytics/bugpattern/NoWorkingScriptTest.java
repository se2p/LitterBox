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
import de.uni_passau.fim.se2.litterbox.analytics.IssueReport;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

public class NoWorkingScriptTest {
    private static Program empty;
    private static Program noWorkingScript;
    private static Program workingScript;
    private static Program myWarrior;
    private static Program noodle;
    private static Program test;
    private static ObjectMapper mapper = new ObjectMapper();

    @BeforeAll
    public static void setUp() throws IOException, ParsingException {

        File f = new File("./src/test/fixtures/emptyProject.json");
        empty = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/bugpattern/noWorkingScript.json");
        noWorkingScript = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/bugpattern/missingPenUp.json");
        workingScript = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/bugpattern/myWarrior.json");
        myWarrior = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/bugpattern/noodle.json");
        noodle = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/356361667.json");
        test = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
    }

    @Test
    public void testEmptyProgram() {
        NoWorkingScripts parameterName = new NoWorkingScripts();
        IssueReport report = parameterName.check(empty);
        Assertions.assertEquals(0, report.getCount());
    }

    @Test
    public void testNoWorkingScript() {
        NoWorkingScripts parameterName = new NoWorkingScripts();
        IssueReport report = parameterName.check(noWorkingScript);
        Assertions.assertEquals(1, report.getCount());
    }

    @Test
    public void testWorkingScript() {
        NoWorkingScripts parameterName = new NoWorkingScripts();
        IssueReport report = parameterName.check(workingScript);
        Assertions.assertEquals(0, report.getCount());
    }

    @Test
    public void testMyWarrior() {
        NoWorkingScripts parameterName = new NoWorkingScripts();
        IssueReport report = parameterName.check(myWarrior);
        Assertions.assertEquals(0, report.getCount());
    }

    @Test
    public void testNoodle() {
        NoWorkingScripts parameterName = new NoWorkingScripts();
        IssueReport report = parameterName.check(noodle);
        Assertions.assertEquals(0, report.getCount());
    }
}
