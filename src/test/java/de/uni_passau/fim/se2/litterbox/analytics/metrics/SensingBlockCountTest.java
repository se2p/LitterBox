/*
 * Copyright (C) 2019-2026 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.analytics.metrics;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class SensingBlockCountTest implements JsonTest {

    @Test
    public void testAll() throws IOException, ParsingException {
        assertThatMetricReports(18, new SensingBlockCount<>(), "./src/test/fixtures/metrics/allSensingBlocks.json");
    }

    @Test
    public void testAverageAll() throws IOException, ParsingException {
        // 18 blocks + 3 menus (not counted as full blocks) in sensing fixture -> assertion 18/21 = 0.85... instead of 1
        assertThatMetricReportsWithin(0.857142857, 1e-4, new SensingBlockCount<>(true), "./src/test/fixtures/metrics/allSensingBlocks.json");
    }

    @Test
    public void testAverageTotal() throws IOException, ParsingException {
        assertThatMetricReportsWithin(0.136363636, 1e-4, new SensingBlockCount<>(true), "./src/test/fixtures/allBlocks.json");
    }
}
