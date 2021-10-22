/*
 * Copyright (C) 2019-2021 LitterBox contributors
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

public class MissingBackdropSwitchTest implements JsonTest {

    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        assertThatFinderReports(0, new MissingBackdropSwitch(), "./src/test/fixtures/emptyProject.json");
    }

    @Test
    public void testMissingBackdropSwitchNext() throws IOException, ParsingException {
        assertThatFinderReports(0, new MissingBackdropSwitch(), "./src/test/fixtures/bugpattern/missingBackDropSwitchNext.json");
    }

    @Test
    public void testMissingBackdropSwitchNextInSprite() throws IOException, ParsingException {
        assertThatFinderReports(0, new MissingBackdropSwitch(), "./src/test/fixtures/bugpattern/missingBackDropSwitchNext2.json");
    }

    @Test
    public void testMissBackdrop() throws IOException, ParsingException {
        assertThatFinderReports(1, new MissingBackdropSwitch(), "./src/test/fixtures/bugpattern/missBackdrop.json");
    }

    @Test
    public void testRandomBack() throws IOException, ParsingException {
        assertThatFinderReports(0, new MissingBackdropSwitch(), "./src/test/fixtures/bugpattern/randomBackdrop.json");
    }

    @Test
    public void testFischmampfer() throws IOException, ParsingException {
        assertThatFinderReports(0, new MissingBackdropSwitch(), "./src/test/fixtures/bugpattern/missingBackdropSwitchAsString.json");
    }

    @Test
    public void testSwitchAndWait() throws IOException, ParsingException {
        assertThatFinderReports(0, new MissingBackdropSwitch(), "./src/test/fixtures/bugpattern/missingBackdropSwitchAndWaitAsString.json");
    }

    @Test
    public void testSwitchWithNumericExpression() throws IOException, ParsingException {
        assertThatFinderReports(0, new MissingBackdropSwitch(), "./src/test/fixtures/bugpattern/missingBackDropSwitchShouldIgnoreNumericExpr.json");
    }

    @Test
    public void testSwitchDuplicate() throws IOException, ParsingException {
        Program programWithSwitch = getAST("./src/test/fixtures/bugpattern/missingBackdropSwitchDouble.json");
        MissingBackdropSwitch parameterName = new MissingBackdropSwitch();
        List<Issue> reports = new ArrayList<>(parameterName.check(programWithSwitch));
        Assertions.assertEquals(2, reports.size());
        Assertions.assertTrue(reports.get(0).isDuplicateOf(reports.get(1)));
    }
}
