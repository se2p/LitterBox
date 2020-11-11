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

public class MissingTerminationConditionTest implements JsonTest {

    @Test
    public void testMissingTermination() throws IOException, ParsingException {
        Program program = getAST("./src/test/fixtures/missingTermination/missingTermination.json");
        Set<Issue> reports = (new MissingTerminationCondition()).check(program);
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void testMissingTerminationNested() throws IOException, ParsingException {
        Program programNested = getAST("./src/test/fixtures/missingTermination/missingTerminationNested.json");
        Set<Issue> reports = (new MissingTerminationCondition()).check(programNested);
        Assertions.assertEquals(1, reports.size());
    }
}
