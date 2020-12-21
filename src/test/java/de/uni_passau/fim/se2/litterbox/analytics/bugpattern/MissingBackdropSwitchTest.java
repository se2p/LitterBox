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

public class MissingBackdropSwitchTest implements JsonTest {

    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        Program empty = getAST("./src/test/fixtures/emptyProject.json");
        MissingBackdropSwitch parameterName = new MissingBackdropSwitch();
        Set<Issue> reports = parameterName.check(empty);
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testMissingBackdropSwitchNext() throws IOException, ParsingException {
        Program missingBackdropSwitchNext = getAST("./src/test/fixtures/bugpattern/missingBackDropSwitchNext.json");
        MissingBackdropSwitch parameterName = new MissingBackdropSwitch();
        Set<Issue> reports = parameterName.check(missingBackdropSwitchNext);
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testMissingBackdropSwitchNextInSprite() throws IOException, ParsingException {
        Program missingBackdropSwitchNext2 = getAST("./src/test/fixtures/bugpattern/missingBackDropSwitchNext2.json");
        MissingBackdropSwitch parameterName = new MissingBackdropSwitch();
        Set<Issue> reports = parameterName.check(missingBackdropSwitchNext2);
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testMissBackdrop() throws IOException, ParsingException {
        Program missingBack = getAST("./src/test/fixtures/bugpattern/missBackdrop.json");
        MissingBackdropSwitch parameterName = new MissingBackdropSwitch();
        Set<Issue> reports = parameterName.check(missingBack);
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void testRandomBack() throws IOException, ParsingException {
        Program random = getAST("./src/test/fixtures/bugpattern/randomBackdrop.json");
        MissingBackdropSwitch parameterName = new MissingBackdropSwitch();
        Set<Issue> reports = parameterName.check(random);
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testFischmampfer() throws IOException, ParsingException {
        Program fischmampfer = getAST("./src/test/fixtures/bugpattern/missingBackdropSwitchAsString.json");
        MissingBackdropSwitch parameterName = new MissingBackdropSwitch();
        Set<Issue> reports = parameterName.check(fischmampfer);
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testSwitchAndWait() throws IOException, ParsingException {
        Program fischmampferWithWait = getAST("./src/test/fixtures/bugpattern/missingBackdropSwitchAndWaitAsString.json");
        MissingBackdropSwitch parameterName = new MissingBackdropSwitch();
        Set<Issue> reports = parameterName.check(fischmampferWithWait);
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testSwitchWithNumericExpression() throws IOException, ParsingException {
        Program programWithSwitch = getAST("./src/test/fixtures/bugpattern/missingBackDropSwitchShouldIgnoreNumericExpr.json");
        MissingBackdropSwitch parameterName = new MissingBackdropSwitch();
        Set<Issue> reports = parameterName.check(programWithSwitch);
        Assertions.assertEquals(0, reports.size());
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
