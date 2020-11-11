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
package de.uni_passau.fim.se2.litterbox.analytics.smells;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

public class VariableInitializationRaceTest implements JsonTest {
    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        Program empty = getAST("./src/test/fixtures/emptyProject.json");

        VariableInitializationRace finder = new VariableInitializationRace();
        Set<Issue> reports = finder.check(empty);
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testVariableInitializationOnGreenFlag() throws IOException, ParsingException {
        Program program = getAST("./src/test/fixtures/smells/raceConditionVarGreenFlag.json");

        VariableInitializationRace finder = new VariableInitializationRace();
        Set<Issue> reports = finder.check(program);
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void testVariableInitializationOn3GreenFlags() throws IOException, ParsingException {
        Program program = getAST("./src/test/fixtures/smells/raceConditionVarGreenFlag3.json");

        VariableInitializationRace finder = new VariableInitializationRace();
        Set<Issue> reports = finder.check(program);
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void testVariableInitializationOnClick() throws IOException, ParsingException {
        Program program = getAST("./src/test/fixtures/smells/raceConditionOnClick.json");

        VariableInitializationRace finder = new VariableInitializationRace();
        Set<Issue> reports = finder.check(program);
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void testVariableInitializationOnDifferentEvents() throws IOException, ParsingException {
        Program program = getAST("./src/test/fixtures/smells/raceConditionDifferentEvents.json");

        VariableInitializationRace finder = new VariableInitializationRace();
        Set<Issue> reports = finder.check(program);
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testVariableInitializationOnDifferentVariables() throws IOException, ParsingException {
        Program program = getAST("./src/test/fixtures/smells/raceConditionDifferentVariables.json");

        VariableInitializationRace finder = new VariableInitializationRace();
        Set<Issue> reports = finder.check(program);
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testVariableInitializationSetAndChangeVariable() throws IOException, ParsingException {
        Program program = getAST("./src/test/fixtures/smells/raceConditionDifferentVariableStatements.json");

        VariableInitializationRace finder = new VariableInitializationRace();
        Set<Issue> reports = finder.check(program);
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void testVariableInitializationDifferentActors() throws IOException, ParsingException {
        Program program = getAST("./src/test/fixtures/smells/raceConditionDifferentActors.json");

        VariableInitializationRace finder = new VariableInitializationRace();
        Set<Issue> reports = finder.check(program);
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void testVariableInitializationLocalVariables() throws IOException, ParsingException {
        Program program = getAST("./src/test/fixtures/smells/raceConditionLocalVariables.json");

        VariableInitializationRace finder = new VariableInitializationRace();
        Set<Issue> reports = finder.check(program);
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testVariableAfterInitializationInLoop() throws IOException, ParsingException {
        Program program = getAST("./src/test/fixtures/smells/raceConditionAfterInitialization.json");

        VariableInitializationRace finder = new VariableInitializationRace();
        Set<Issue> reports = finder.check(program);
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testVariableAfterInitialization() throws IOException, ParsingException {
        Program program = getAST("./src/test/fixtures/smells/raceConditionAfterInitialization2.json");

        VariableInitializationRace finder = new VariableInitializationRace();
        Set<Issue> reports = finder.check(program);
        Assertions.assertEquals(0, reports.size());
    }
}
