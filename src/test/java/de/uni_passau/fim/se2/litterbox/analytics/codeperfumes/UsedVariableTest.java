/*
 * Copyright (C) 2019-2024 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.analytics.codeperfumes;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.bugpattern.MissingInitialization;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UsedVariableTest implements JsonTest {

    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        assertThatFinderReports(0, new UsedVariables(), "./src/test/fixtures/emptyProject.json");
    }

    @Test
    public void testReal() throws IOException, ParsingException {
        assertThatFinderReports(1, new UsedVariables(), "./src/test/fixtures/goodPractice/usedVariable.json");
    }

    @Test
    public void testUsedVariableUninitialized() throws IOException, ParsingException {
        Program prog = JsonTest.parseProgram("./src/test/fixtures/goodPractice/usedVariableUninitialized.json");
        MissingInitialization missingInit = new MissingInitialization();
        List<Issue> issues = new ArrayList<>(missingInit.check(prog));
        Assertions.assertEquals(1, issues.size());

        UsedVariables usedVariables = new UsedVariables();
        List<Issue> issuesUsedVariables = new ArrayList<>(usedVariables.check(prog));
        Assertions.assertEquals(1, issuesUsedVariables.size());

        Assertions.assertTrue(issuesUsedVariables.getFirst().isSubsumedBy(issues.getFirst()));
    }
}
