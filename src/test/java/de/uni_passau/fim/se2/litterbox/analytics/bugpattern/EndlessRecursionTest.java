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

import static com.google.common.truth.Truth.assertThat;

public class EndlessRecursionTest {
    private static Program empty;
    private static Program endlessRecursion;
    private static Program recursion;
    private static ObjectMapper mapper = new ObjectMapper();

    @BeforeAll
    public static void setUp() throws IOException, ParsingException {

        File f = new File("./src/test/fixtures/emptyProject.json");
        empty = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/bugpattern/recursiveProcedure.json");
        endlessRecursion = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/bugpattern/recursion.json");
        recursion = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
    }

    @Test
    public void testEmptyProgram() {
        EndlessRecursion parameterName = new EndlessRecursion();
        Set<Issue> reports = parameterName.check(empty);
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testEndlessRecursion() {
        EndlessRecursion parameterName = new EndlessRecursion();
        Set<Issue> reports = parameterName.check(endlessRecursion);
        Assertions.assertEquals(1, reports.size());

        // Check the procedure is correctly set
        Issue issue = reports.iterator().next();
        assertThat(issue.getProcedure().getIdent().getName()).isEqualTo("u(.tD,]^yo1^B?8TSwez");
    }

    @Test
    public void testRecursion() {
        EndlessRecursion parameterName = new EndlessRecursion();
        Set<Issue> reports = parameterName.check(recursion);
        Assertions.assertEquals(0, reports.size());
    }
}
