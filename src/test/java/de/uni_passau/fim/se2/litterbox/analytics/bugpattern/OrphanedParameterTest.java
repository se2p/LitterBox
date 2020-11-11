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
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public class OrphanedParameterTest implements JsonTest {

    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        Program empty = JsonTest.parseProgram("./src/test/fixtures/emptyProject.json");
        OrphanedParameter parameterName = new OrphanedParameter();
        Set<Issue> reports = parameterName.check(empty);
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testOrphanedParameter() throws IOException, ParsingException {
        Program orphanedParam = JsonTest.parseProgram("./src/test/fixtures/bugpattern/orphanedParameter.json");
        OrphanedParameter parameterName = new OrphanedParameter();
        Set<Issue> reports = parameterName.check(orphanedParam);
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void testOutsideParameter() throws IOException, ParsingException {
        Program outsideParam = JsonTest.parseProgram("./src/test/fixtures/bugpattern/parameterOutsideScope.json");
        OrphanedParameter parameterName = new OrphanedParameter();
        Set<Issue> reports = parameterName.check(outsideParam);
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testOrphanedParameterRichtung() throws IOException, ParsingException {
        Program orphanedParam = JsonTest.parseProgram("./src/test/fixtures/bugpattern/orphanedParamRichtung.json");
        OrphanedParameter orph = new OrphanedParameter();
        Set<Issue> reports = orph.check(orphanedParam);
        Assertions.assertEquals(1, reports.size());
    }
}
