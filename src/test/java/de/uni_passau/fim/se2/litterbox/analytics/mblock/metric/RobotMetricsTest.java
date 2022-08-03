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
package de.uni_passau.fim.se2.litterbox.analytics.mblock.metric;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class RobotMetricsTest implements JsonTest {

    @Test
    public void testRobotCodeMetric() throws ParsingException, IOException {
        assertThatMetricReports(1, new RobotCodeMetric(), "./src/test/fixtures/mblock/mBlock_all_variants.json");
    }

    @Test
    public void testCodeyCounterMetric() throws ParsingException, IOException {
        assertThatMetricReports(8, new CodeyCounterMetric(), "./src/test/fixtures/mblock/mBlock_all_variants.json");
    }

    @Test
    public void testMCoreCounterMetric() throws ParsingException, IOException {
        assertThatMetricReports(1, new MCoreCounterMetric(), "./src/test/fixtures/mblock/mBlock_all_variants.json");
    }

    @Test
    public void testAurigaCounterMetric() throws ParsingException, IOException {
        assertThatMetricReports(1, new AurigaCounterMetric(), "./src/test/fixtures/mblock/failed/421199.json");
    }

    @Test
    public void testRobotCodeMetricNone() throws ParsingException, IOException {
        assertThatMetricReports(0, new RobotCodeMetric(), "./src/test/fixtures/allBlocks.json");
    }

    @Test
    public void testCodeyCounterMetricNone() throws ParsingException, IOException {
        assertThatMetricReports(0, new CodeyCounterMetric(), "./src/test/fixtures/allBlocks.json");
    }

    @Test
    public void testMCoreCounterMetricNone() throws ParsingException, IOException {
        assertThatMetricReports(0, new MCoreCounterMetric(), "./src/test/fixtures/allBlocks.json");
    }

    @Test
    public void testAurigaCounterMetricNone() throws ParsingException, IOException {
        assertThatMetricReports(0, new AurigaCounterMetric(), "./src/test/fixtures/mblock/mBlock_all_variants.json");
    }

    @Test
    public void testMegapiCounterMetricNone() throws ParsingException, IOException {
        assertThatMetricReports(0, new MegapiCounterMetric(), "./src/test/fixtures/mblock/mBlock_all_variants.json");
    }
}
