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

import com.fasterxml.jackson.databind.ObjectMapper;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public class VariableInitializationRaceTest {
    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        File f = new File("./src/test/fixtures/emptyProject.json");
        Program empty = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));

        VariableInitializationRace finder = new VariableInitializationRace();
        Set<Issue> reports = finder.check(empty);
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testVariableInitializationOnGreenFlag() throws IOException, ParsingException {
        File f = new File("./src/test/fixtures/smells/raceConditionVarGreenFlag.json");
        Program program = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));

        VariableInitializationRace finder = new VariableInitializationRace();
        Set<Issue> reports = finder.check(program);
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void testVariableInitializationOn3GreenFlags() throws IOException, ParsingException {
        File f = new File("./src/test/fixtures/smells/raceConditionVarGreenFlag3.json");
        Program program = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));

        VariableInitializationRace finder = new VariableInitializationRace();
        Set<Issue> reports = finder.check(program);
        Assertions.assertEquals(1, reports.size());
    }


    @Test
    public void testVariableInitializationOnClick() throws IOException, ParsingException {
        File f = new File("./src/test/fixtures/smells/raceConditionOnClick.json");
        Program program = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));

        VariableInitializationRace finder = new VariableInitializationRace();
        Set<Issue> reports = finder.check(program);
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void testVariableInitializationOnDifferentEvents() throws IOException, ParsingException {
        File f = new File("./src/test/fixtures/smells/raceConditionDifferentEvents.json");
        Program program = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));

        VariableInitializationRace finder = new VariableInitializationRace();
        Set<Issue> reports = finder.check(program);
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testVariableInitializationOnDifferentVariables() throws IOException, ParsingException {
        File f = new File("./src/test/fixtures/smells/raceConditionDifferentVariables.json");
        Program program = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));

        VariableInitializationRace finder = new VariableInitializationRace();
        Set<Issue> reports = finder.check(program);
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testVariableInitializationSetAndChangeVariable() throws IOException, ParsingException {
        File f = new File("./src/test/fixtures/smells/raceConditionDifferentVariableStatements.json");
        Program program = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));

        VariableInitializationRace finder = new VariableInitializationRace();
        Set<Issue> reports = finder.check(program);
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void testVariableInitializationDifferentActors() throws IOException, ParsingException {
        File f = new File("./src/test/fixtures/smells/raceConditionDifferentActors.json");
        Program program = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));

        VariableInitializationRace finder = new VariableInitializationRace();
        Set<Issue> reports = finder.check(program);
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void testVariableInitializationLocalVariables() throws IOException, ParsingException {
        File f = new File("./src/test/fixtures/smells/raceConditionLocalVariables.json");
        Program program = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));

        VariableInitializationRace finder = new VariableInitializationRace();
        Set<Issue> reports = finder.check(program);
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testVariableAfterInitializationInLoop() throws IOException, ParsingException {
        File f = new File("./src/test/fixtures/smells/raceConditionAfterInitialization.json");
        Program program = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));

        VariableInitializationRace finder = new VariableInitializationRace();
        Set<Issue> reports = finder.check(program);
        Assertions.assertEquals(0, reports.size());
    }
    
    @Test
    public void testVariableAfterInitialization() throws IOException, ParsingException {
        File f = new File("./src/test/fixtures/smells/raceConditionAfterInitialization2.json");
        Program program = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));

        VariableInitializationRace finder = new VariableInitializationRace();
        Set<Issue> reports = finder.check(program);
        Assertions.assertEquals(0, reports.size());
    }
}
