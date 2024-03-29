/*
 * Copyright (C) 2019-2022 LitterBox contributors
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
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CoordinationTest implements JsonTest {

    @Test
    public void testCoordination() throws IOException, ParsingException {
        assertThatFinderReports(1, new Coordination(), "./src/test/fixtures/goodPractice/coordinationBoth.json");
    }

    @Test
    public void testSubsumption() throws IOException, ParsingException {
        Program prog = JsonTest.parseProgram("./src/test/fixtures/goodPractice/oneTimeCheckStop.json");
        WaitingCheckToStop waitingCheckToStop = new WaitingCheckToStop();
        List<Issue> report = new ArrayList<>(waitingCheckToStop.check(prog));
        Assertions.assertEquals(1, report.size());
        Coordination coordination = new Coordination();
        List<Issue> reportCoord = new ArrayList<>(coordination.check(prog));
        Assertions.assertEquals(1, reportCoord.size());
        Assertions.assertTrue(reportCoord.get(0).isSubsumedBy(report.get(0)));
    }
}
