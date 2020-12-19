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
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

public class MissingLoopSensingTest implements JsonTest {

    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        Program empty = JsonTest.parseProgram("./src/test/fixtures/emptyProject.json");
        MissingLoopSensing parameterName = new MissingLoopSensing();
        Set<Issue> reports = parameterName.check(empty);
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testMissingLoopSensing() throws IOException, ParsingException {
        Program codeHero = JsonTest.parseProgram("./src/test/fixtures/bugpattern/codeHero.json");
        MissingLoopSensing parameterName = new MissingLoopSensing();
        Set<Issue> reports = parameterName.check(codeHero);
        Assertions.assertEquals(2, reports.size());
    }

    @Test
    public void testAnina() throws IOException, ParsingException {
        Program anina = JsonTest.parseProgram("./src/test/fixtures/bugpattern/anina.json");
        MissingLoopSensing parameterName = new MissingLoopSensing();
        Set<Issue> reports = parameterName.check(anina);
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void testMissingLoopSensingNested() throws IOException, ParsingException {
        Program nestedMissingLoopSensing = JsonTest.parseProgram("./src/test/fixtures/bugpattern/nestedMissingLoopSensing.json");
        MissingLoopSensing parameterName = new MissingLoopSensing();
        Set<Issue> reports = parameterName.check(nestedMissingLoopSensing);
        Assertions.assertEquals(2, reports.size());
    }

    @Test
    public void testMissingLoopSensingMultiple() throws IOException, ParsingException {
        Program missingLoopSensingMultiple = JsonTest.parseProgram("./src/test/fixtures/bugpattern/missingLoopSensingMultiple.json");
        MissingLoopSensing parameterName = new MissingLoopSensing();
        Set<Issue> reports = parameterName.check(missingLoopSensingMultiple);
        Assertions.assertEquals(5, reports.size());
    }

    @Test
    public void testMissingLoopSensingVariable() throws IOException, ParsingException {
        Program missingLoopSensingVariable = JsonTest.parseProgram("./src/test/fixtures/bugpattern/geisterwald.json");
        MissingLoopSensing parameterName = new MissingLoopSensing();
        Set<Issue> reports = parameterName.check(missingLoopSensingVariable);
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void testGetName() {
        MissingLoopSensing parameterName = new MissingLoopSensing();
        Assertions.assertEquals("missing_loop_sensing", parameterName.getName());
    }

    @Test
    public void testMissingLoopSensingAfterWaitUntil() throws IOException, ParsingException {
        Program empty = JsonTest.parseProgram("./src/test/fixtures/bugpattern/missingLoopSensingAfterWaitUntil.json");
        MissingLoopSensing parameterName = new MissingLoopSensing();
        Set<Issue> reports = parameterName.check(empty);
        Assertions.assertEquals(0, reports.size());
    }
}
