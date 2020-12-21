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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class StutteringMovementTest implements JsonTest {

    @Test
    public void testStutteringMovement() throws IOException, ParsingException {
        Program stutteringMovement = getAST("./src/test/fixtures/bugpattern/stutteringMovement.json");
        StutteringMovement finder = new StutteringMovement();
        Set<Issue> reports = finder.check(stutteringMovement);
        Assertions.assertEquals(3, reports.size());
    }

    @Test
    public void testDeleteParam() throws IOException, ParsingException {
        Program deleteParam = getAST("./src/test/fixtures/stmtParser/deleteParam.json");
        StutteringMovement finder = new StutteringMovement();
        Set<Issue> reports = finder.check(deleteParam);
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testStutteringRotation() throws IOException, ParsingException {
        Program stutteringMovement = getAST("./src/test/fixtures/bugpattern/stutteringRotation.json");
        StutteringMovement finder = new StutteringMovement();
        Set<Issue> reports = finder.check(stutteringMovement);
        Assertions.assertEquals(2, reports.size());
    }

    @Test
    public void testStutteringMovementReset() throws IOException, ParsingException {
        Program stutteringMovement = getAST("./src/test/fixtures/bugpattern/stutteringMovementBug.json");
        StutteringMovement finder = new StutteringMovement();
        Set<Issue> reports = finder.check(stutteringMovement);
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void testStutteringMovementDuplicates() throws IOException, ParsingException {
        Program stutteringMovement = getAST("./src/test/fixtures/bugpattern/doubleStuttering.json");
        StutteringMovement finder = new StutteringMovement();
        List<Issue> reports = new ArrayList<>(finder.check(stutteringMovement));
        Assertions.assertEquals(2, reports.size());
        Assertions.assertTrue(reports.get(0).isDuplicateOf(reports.get(1)));
    }
}
