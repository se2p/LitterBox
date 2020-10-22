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

public class MissingLoopSensingTest implements JsonTest {

    private static Program empty;
    private static Program codeHero;
    private static Program anina;
    private static Program nestedMissingLoopSensing;
    private static Program missingLoopSensingMultiple;

    @BeforeAll
    public static void setUp() throws IOException, ParsingException {

        empty = JsonTest.parseProgram("./src/test/fixtures/emptyProject.json");
        codeHero = JsonTest.parseProgram("./src/test/fixtures/bugpattern/codeHero.json");
        anina = JsonTest.parseProgram("./src/test/fixtures/bugpattern/anina.json");
        nestedMissingLoopSensing = JsonTest.parseProgram("./src/test/fixtures/bugpattern/nestedMissingLoopSensing.json");
        missingLoopSensingMultiple = JsonTest.parseProgram("./src/test/fixtures/bugpattern/missingLoopSensingMultiple.json");
    }

    @Test
    public void testEmptyProgram() {
        MissingLoopSensing parameterName = new MissingLoopSensing();
        Set<Issue> reports = parameterName.check(empty);
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testMissingLoopSensing() {
        MissingLoopSensing parameterName = new MissingLoopSensing();
        Set<Issue> reports = parameterName.check(codeHero);
        Assertions.assertEquals(2, reports.size());
    }

    @Test
    public void testAnina() {
        MissingLoopSensing parameterName = new MissingLoopSensing();
        Set<Issue> reports = parameterName.check(anina);
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void testMissingLoopSensingNested() {
        MissingLoopSensing parameterName = new MissingLoopSensing();
        Set<Issue> reports = parameterName.check(nestedMissingLoopSensing);
        Assertions.assertEquals(2, reports.size());
    }

    @Test
    public void testMissingLoopSensingMultiple() {
        MissingLoopSensing parameterName = new MissingLoopSensing();
        Set<Issue> reports = parameterName.check(missingLoopSensingMultiple);
        Assertions.assertEquals(5, reports.size());
    }

    @Test
    public void testGetName() {
        MissingLoopSensing parameterName = new MissingLoopSensing();
        Assertions.assertEquals("missing_loop_sensing", parameterName.getName());
    }
}
