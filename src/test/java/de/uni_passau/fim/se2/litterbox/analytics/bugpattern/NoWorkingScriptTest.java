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

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

public class NoWorkingScriptTest implements JsonTest {
    private static Program empty;
    private static Program noWorkingScript;
    private static Program workingScript;
    private static Program myWarrior;
    private static Program noodle;

    @BeforeAll
    public static void setUp() throws IOException, ParsingException {

        empty = JsonTest.parseProgram("./src/test/fixtures/emptyProject.json");
        noWorkingScript = JsonTest.parseProgram("./src/test/fixtures/bugpattern/noWorkingScript.json");
        workingScript = JsonTest.parseProgram("./src/test/fixtures/bugpattern/missingPenUp.json");
        myWarrior = JsonTest.parseProgram("./src/test/fixtures/bugpattern/myWarrior.json");
        noodle = JsonTest.parseProgram("./src/test/fixtures/bugpattern/noodle.json");
    }

    @Test
    public void testEmptyProgram() {
        NoWorkingScripts parameterName = new NoWorkingScripts();
        Set<Issue> reports = parameterName.check(empty);
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testNoWorkingScript() {
        NoWorkingScripts parameterName = new NoWorkingScripts();
        Set<Issue> reports = parameterName.check(noWorkingScript);
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void testWorkingScript() {
        NoWorkingScripts parameterName = new NoWorkingScripts();
        Set<Issue> reports = parameterName.check(workingScript);
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testMyWarrior() {
        NoWorkingScripts parameterName = new NoWorkingScripts();
        Set<Issue> reports = parameterName.check(myWarrior);
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testNoodle() {
        NoWorkingScripts parameterName = new NoWorkingScripts();
        Set<Issue> reports = parameterName.check(noodle);
        Assertions.assertEquals(0, reports.size());
    }
}
