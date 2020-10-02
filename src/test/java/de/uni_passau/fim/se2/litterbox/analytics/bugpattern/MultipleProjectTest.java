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

public class MultipleProjectTest implements JsonTest {

    private static Program boatrace;
    private static Program ballgame;

    @BeforeAll
    public static void setUp() throws IOException, ParsingException {

        boatrace = JsonTest.parseProgram("./src/test/fixtures/bugpattern/boatrace.json");
        ballgame = JsonTest.parseProgram("./src/test/fixtures/bugpattern/ballgame.json");
    }

    @Test
    public void testBoatRace() {
        MissingLoopSensing parameterName = new MissingLoopSensing();
        Set<Issue> reports = parameterName.check(boatrace);
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testBallgame() {
        MissingLoopSensing parameterName = new MissingLoopSensing();
        Set<Issue> reports = parameterName.check(ballgame);
        Assertions.assertEquals(2, reports.size());
    }

    @Test
    public void testCombined() {
        MissingLoopSensing parameterName = new MissingLoopSensing();
        Set<Issue> reports = parameterName.check(ballgame);
        Assertions.assertEquals(2, reports.size());
        Set<Issue> reports2 = parameterName.check(boatrace);
        Assertions.assertEquals(0, reports2.size());
    }
}
