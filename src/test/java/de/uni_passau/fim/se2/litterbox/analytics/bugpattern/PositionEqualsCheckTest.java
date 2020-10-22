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
import java.util.Set;

public class PositionEqualsCheckTest implements JsonTest {

    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        Program empty = JsonTest.parseProgram("./src/test/fixtures/emptyProject.json");
        PositionEqualsCheck parameterName = new PositionEqualsCheck();
        Set<Issue> reports = parameterName.check(empty);
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testEqualCond() throws IOException, ParsingException {
        Program equalX = JsonTest.parseProgram("./src/test/fixtures/bugpattern/xPosEqual.json");
        PositionEqualsCheck parameterName = new PositionEqualsCheck();
        Set<Issue> reports = parameterName.check(equalX);
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void testEqualDir() throws IOException, ParsingException {
        Program equalDirection = JsonTest.parseProgram("./src/test/fixtures/bugpattern/posEqualDirection.json");
        PositionEqualsCheck parameterName = new PositionEqualsCheck();
        Set<Issue> reports = parameterName.check(equalDirection);
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testXPositionEquals() throws IOException, ParsingException {
        Program xPositionEquals = JsonTest.parseProgram("./src/test/fixtures/bugpattern/xPositionEquals.json");
        PositionEqualsCheck parameterName = new PositionEqualsCheck();
        Set<Issue> reports = parameterName.check(xPositionEquals);
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void testAll() throws IOException, ParsingException {
        Program allChecks = JsonTest.parseProgram("./src/test/fixtures/bugpattern/positionEqualsCheck.json");
        PositionEqualsCheck parameterName = new PositionEqualsCheck();
        Set<Issue> reports = parameterName.check(allChecks);
        Assertions.assertEquals(4, reports.size());
    }

    @Test
    public void testNested() throws IOException, ParsingException {
        Program nested = JsonTest.parseProgram("./src/test/fixtures/bugpattern/positionEqualsNested.json");
        PositionEqualsCheck parameterName = new PositionEqualsCheck();
        Set<Issue> reports = parameterName.check(nested);
        Assertions.assertEquals(2, reports.size());
    }

    @Test
    public void testDeadEquals() throws IOException, ParsingException {
        Program deadEquals = JsonTest.parseProgram("./src/test/fixtures/bugpattern/deadPositionEquals.json");
        PositionEqualsCheck parameterName = new PositionEqualsCheck();
        Set<Issue> reports = parameterName.check(deadEquals);
        Assertions.assertEquals(2, reports.size());
    }

    @Test
    public void testDistanceZero() throws IOException, ParsingException {
        Program deadEquals = JsonTest.parseProgram("./src/test/fixtures/bugpattern/pecDistanceZero.json");
        PositionEqualsCheck parameterName = new PositionEqualsCheck();
        Set<Issue> reports = parameterName.check(deadEquals);
        Assertions.assertEquals(1, reports.size());
        Hint hint = new Hint(PositionEqualsCheckHintFactory.DISTANCE_ZERO_SPRITES);
        for (Issue issue : reports) {
            Truth.assertThat(issue.getHint()).isEqualTo(hint.getHintText());
        }
    }
}
