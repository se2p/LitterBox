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
package de.uni_passau.fim.se2.litterbox.analytics.fix_heuristics;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class MissingLoopSensingWaitFixTest implements JsonTest {

    @Test
    public void testLoopSensingFix() throws IOException, ParsingException {
        assertThatFinderReports(1, new MissingLoopSensingWaitFix("NG5_c]Sc%NFgdLaV]%Cq"), "./src/test/fixtures/fix_heuristics/missingLoopSensingStopWait.json");
    }

    @Test
    public void testLoopSensingWithoutStopFix() throws IOException, ParsingException {
        assertThatFinderReports(1, new MissingLoopSensingWaitFix("9/_j,gpuA,GxfN~R%y_z"), "./src/test/fixtures/fix_heuristics/missingLoopSensingFixWithoutStop.json");
    }
}
