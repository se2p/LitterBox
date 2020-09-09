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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public class UnusedIdentifierTest {
    private static Program empty;
    private static Program unusedVariables;
    private static Program oneUsedVariables;
    private static Program usedList;
    private static Program unusedList;
    private static ObjectMapper mapper = new ObjectMapper();

    @BeforeAll
    public static void setUp() throws IOException, ParsingException {

        File f = new File("./src/test/fixtures/bugpattern/recursion.json");
        empty = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/smells/unusedVariables.json");
        unusedVariables = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/smells/oneUsedVariable.json");
        oneUsedVariables = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/smells/listunused.json");
        unusedList = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/smells/listused.json");
        usedList = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
    }

    @Test
    public void testEmptyProgram() {
        UnusedVariable parameterName = new UnusedVariable();
        Set<Issue> reports = parameterName.check(empty);
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testUnusedVariable() {
        UnusedVariable parameterName = new UnusedVariable();
        Set<Issue> reports = parameterName.check(unusedVariables);
        Assertions.assertEquals(2, reports.size());
    }

    @Test
    public void testOneUsedVariable() {
        UnusedVariable parameterName = new UnusedVariable();
        Set<Issue> reports = parameterName.check(oneUsedVariables);
        Assertions.assertEquals(2, reports.size());
    }

    @Test
    public void testUnusedList() {
        UnusedVariable parameterName = new UnusedVariable();
        Set<Issue> reports = parameterName.check(unusedList);
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void testUsedList() {
        UnusedVariable parameterName = new UnusedVariable();
        Set<Issue> reports = parameterName.check(usedList);
        Assertions.assertEquals(0, reports.size());
    }
}
