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
package de.uni_passau.fim.se2.litterbox.analytics.bugpattern;

import com.google.common.truth.Truth;
import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.hint.PositionEqualsCheckHintFactory;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PositionEqualsCheckTest implements JsonTest {

    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        assertThatFinderReports(0, new PositionEqualsCheck(), "./src/test/fixtures/emptyProject.json");
    }

    @Test
    public void testEqualCond() throws IOException, ParsingException {
        assertThatFinderReports(1, new PositionEqualsCheck(), "./src/test/fixtures/bugpattern/xPosEqual.json");
    }

    @Test
    public void testEqualDir() throws IOException, ParsingException {
        assertThatFinderReports(0, new PositionEqualsCheck(), "./src/test/fixtures/bugpattern/posEqualDirection.json");
    }

    @Test
    public void testXPositionEquals() throws IOException, ParsingException {
        assertThatFinderReports(1, new PositionEqualsCheck(), "./src/test/fixtures/bugpattern/xPositionEquals.json");
    }

    @Test
    public void testAll() throws IOException, ParsingException {
        assertThatFinderReports(4, new PositionEqualsCheck(), "./src/test/fixtures/bugpattern/positionEqualsCheck.json");
    }

    @Test
    public void testNested() throws IOException, ParsingException {
        assertThatFinderReports(2, new PositionEqualsCheck(), "./src/test/fixtures/bugpattern/positionEqualsNested.json");
    }

    @Test
    public void testDeadEquals() throws IOException, ParsingException {
        assertThatFinderReports(2, new PositionEqualsCheck(), "./src/test/fixtures/bugpattern/deadPositionEquals.json");
    }

    @Test
    public void testDistanceZero() throws IOException, ParsingException {
        Program deadEquals = JsonTest.parseProgram("./src/test/fixtures/bugpattern/pecDistanceZero.json");
        PositionEqualsCheck parameterName = new PositionEqualsCheck();
        Set<Issue> reports = parameterName.check(deadEquals);
        Assertions.assertEquals(1, reports.size());
        Hint hint = Hint.fromKey(PositionEqualsCheckHintFactory.DISTANCE_ZERO_SPRITES);
        for (Issue issue : reports) {
            Truth.assertThat(issue.getHintText()).isEqualTo(hint.getHintText());
        }
    }

    @Test
    public void testSubsumptionDistanceTo() throws IOException, ParsingException {
        Program prog = JsonTest.parseProgram("./src/test/fixtures/bugpattern/distanceFromColorPEC.json");
        PositionEqualsCheck parameterName = new PositionEqualsCheck();
        List<Issue> reportsPEC = new ArrayList<>(parameterName.check(prog));
        Assertions.assertEquals(1, reportsPEC.size());
        TypeError typeError = new TypeError();
        List<Issue> reportsType = new ArrayList<>(typeError.check(prog));
        Assertions.assertEquals(1, reportsType.size());
        Assertions.assertTrue(reportsPEC.get(0).isSubsumedBy(reportsType.get(0)));
    }
}
