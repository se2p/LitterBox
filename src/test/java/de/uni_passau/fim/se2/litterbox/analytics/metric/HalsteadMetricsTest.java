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
package de.uni_passau.fim.se2.litterbox.analytics.metric;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class HalsteadMetricsTest implements JsonTest {

    @Test
    public void testLength() throws IOException, ParsingException {
        assertThatMetricReports(12, new HalsteadLength(), "src/test/fixtures/cfg/ifelse_repeattimes.json");
    }

    @Test
    public void testSize() throws IOException, ParsingException {
        assertThatMetricReports(7, new HalsteadVocabulary(), "src/test/fixtures/cfg/ifelse_repeattimes.json");
    }

    @Test
    public void testVolume() throws IOException, ParsingException {
        // V = N * log2(n)
        assertThatMetricReportsWithin(33.7, 0.1, new HalsteadVolume(), "src/test/fixtures/cfg/ifelse_repeattimes.json");
    }

    @Test
    public void testDifficulty() throws IOException, ParsingException {
        //  D = ( n1 / 2 ) * ( N2 / n2 )
        assertThatMetricReports(6.25, new HalsteadDifficulty(), "src/test/fixtures/cfg/ifelse_repeattimes.json");
    }

    @Test
    public void testEffort() throws IOException, ParsingException {
        // E = V * D
        assertThatMetricReportsWithin(210.6, 0.1, new HalsteadEffort(), "src/test/fixtures/cfg/ifelse_repeattimes.json");
    }

    @Test
    public void testNoDivisionByZeroWithoutOperands_Effort() throws IOException, ParsingException {
        assertThatMetricReports(0, new HalsteadEffort(), "src/test/fixtures/metrics/halstead_bug.json");
    }

    @Test
    public void testNoDivisionByZeroWithoutOperands_Difficulty() throws IOException, ParsingException {
        assertThatMetricReports(0, new HalsteadDifficulty(), "src/test/fixtures/metrics/halstead_bug.json");
    }

    @Test
    public void testVolume_EmptyProject() throws IOException, ParsingException {
        assertThatMetricReports(0, new HalsteadVolume(), "src/test/fixtures/metrics/empty.json");
    }
}
