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

import static com.google.common.truth.Truth.assertThat;

public class EndlessRecursionTest implements JsonTest {

    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        Program empty = getAST("./src/test/fixtures/emptyProject.json");
        EndlessRecursion parameterName = new EndlessRecursion();
        Set<Issue> reports = parameterName.check(empty);
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testEndlessRecursion() throws IOException, ParsingException {
        Program endlessRecursion = getAST("./src/test/fixtures/bugpattern/recursiveProcedure.json");
        EndlessRecursion parameterName = new EndlessRecursion();
        Set<Issue> reports = parameterName.check(endlessRecursion);
        Assertions.assertEquals(1, reports.size());

        // Check the procedure is correctly set
        Issue issue = reports.iterator().next();
        assertThat(issue.getProcedure().getIdent().getName()).isEqualTo("u(.tD,]^yo1^B?8TSwez");
    }

    @Test
    public void testRecursion() throws IOException, ParsingException {
        Program recursion = getAST("./src/test/fixtures/bugpattern/recursion.json");
        EndlessRecursion parameterName = new EndlessRecursion();
        Set<Issue> reports = parameterName.check(recursion);
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testEndlessBroadcast() throws IOException, ParsingException {
        Program endlessRecursion = getAST("./src/test/fixtures/bugpattern/endlessBroadcast.json");
        EndlessRecursion parameterName = new EndlessRecursion();
        Set<Issue> reports = parameterName.check(endlessRecursion);
        Assertions.assertEquals(1, reports.size());
    }
}
