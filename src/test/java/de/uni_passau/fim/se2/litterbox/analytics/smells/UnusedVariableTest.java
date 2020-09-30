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

public class UnusedVariableTest implements JsonTest {

    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        Program empty = getAST("./src/test/fixtures/bugpattern/recursion.json");
        UnusedVariable parameterName = new UnusedVariable();
        Set<Issue> reports = parameterName.check(empty);
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testUnusedVariable() throws IOException, ParsingException {
        Program unusedVariables = getAST("./src/test/fixtures/smells/unusedVariables.json");
        UnusedVariable parameterName = new UnusedVariable();
        Set<Issue> reports = parameterName.check(unusedVariables);
        Assertions.assertEquals(2, reports.size());
    }

    @Test
    public void testOneUsedVariable() throws IOException, ParsingException {
        Program oneUsedVariables = getAST("./src/test/fixtures/smells/oneUsedVariable.json");
        UnusedVariable parameterName = new UnusedVariable();
        Set<Issue> reports = parameterName.check(oneUsedVariables);
        Assertions.assertEquals(2, reports.size());
    }

    @Test
    public void testUnusedList() throws IOException, ParsingException {
        Program unusedList = getAST("./src/test/fixtures/smells/listunused.json");
        UnusedVariable parameterName = new UnusedVariable();
        Set<Issue> reports = parameterName.check(unusedList);
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void testUsedList() throws IOException, ParsingException {
        Program usedList = getAST("./src/test/fixtures/smells/listused.json");
        UnusedVariable parameterName = new UnusedVariable();
        Set<Issue> reports = parameterName.check(usedList);
        Assertions.assertEquals(0, reports.size());
    }
}
