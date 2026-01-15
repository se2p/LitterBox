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

public class LiteralCountTest implements JsonTest {

    @Test
    public void testAllMotion() throws IOException, ParsingException {
        assertThatMetricReports(14, new LiteralCount<>(), "./src/test/fixtures/metrics/allMotionBlocks.json");
    }

    @Test
    public void testAllLooks() throws IOException, ParsingException {
        assertThatMetricReports(11, new LiteralCount<>(), "./src/test/fixtures/metrics/allLooksBlocks.json");
    }

    @Test
    public void testAllSound() throws IOException, ParsingException {
        assertThatMetricReports(4, new LiteralCount<>(), "./src/test/fixtures/metrics/allSoundsBlocks.json");
    }

    @Test
    public void testAverageMotion() throws IOException, ParsingException {
        assertThatMetricReports(0.6666666666666666, new LiteralCount<>(true), "./src/test/fixtures/metrics/allMotionBlocks.json");
    }
}
