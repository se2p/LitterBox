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
package de.uni_passau.fim.se2.litterbox.analytics.metric;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class TokenEntropyTest implements JsonTest {

    @Test
    public void testFourBlocks() throws IOException, ParsingException {
        double p1 = 1.0 / 7.0;
        double p2 = 3.0 / 7.0;
        double expectedEntropy = -(p1 * (Math.log(p1) / Math.log(2.0)) + 2 * p2 * (Math.log(p2) / Math.log(2.0)));

        assertThatMetricReportsWithin(expectedEntropy, 0.1, new TokenEntropy<>(), "src/test/fixtures/metrics/entropy_3identical.json");
    }

    @Test
    public void testNestedBlocks() throws IOException, ParsingException {
        double p1 = 1.0 / 11.0;
        double p3 = 3.0 / 11.0;
        double expectedEntropy = -(5 * p1 * (Math.log(p1) / Math.log(2.0)) +
                (2 * p3 * (Math.log(p3) / Math.log(2.0))));

        assertThatMetricReportsWithin(expectedEntropy, 0.1, new TokenEntropy<>(), "src/test/fixtures/metrics/entropy_nestedblocks.json");
    }

    @Test
    public void testCustomBlock() throws IOException, ParsingException {
        double p1 = 1.0 / 23.0;
        double p3 = 3.0 / 23.0;
        double expectedEntropy = -(17 * p1 * (Math.log(p1) / Math.log(2.0)) +
                2 * p3 * (Math.log(p3) / Math.log(2.0)));

        assertThatMetricReportsWithin(expectedEntropy, 0.1, new TokenEntropy<>(), "src/test/fixtures/metrics/entropy_customblock.json");
    }

    @Test
    public void testIfElse() throws IOException, ParsingException {
        double p1 = 1.0 / 12.0;
        double p2 = 2.0 / 12.0;
        double p4 = 4.0 / 12.0;
        double expectedEntropy = -(4 * p1 * (Math.log(p1) / Math.log(2.0)) +
                2 * p2 * (Math.log(p2) / Math.log(2.0)) +
                p4 * (Math.log(p4) / Math.log(2.0)));

        assertThatMetricReportsWithin(expectedEntropy, 0.1, new TokenEntropy<>(), "src/test/fixtures/cfg/ifelse_repeattimes.json");
    }

    @Test
    public void testMultipleScripts() throws IOException, ParsingException {
        double p = 1.0 / 14.0;
        double expectedEntropy = -(14 * p * (Math.log(p) / Math.log(2.0)));

        assertThatMetricReportsWithin(expectedEntropy, 0.1, new TokenEntropy<>(), "./src/test/fixtures/weightedMethod.json");
    }

    @Test
    public void testComplexScript() throws IOException, ParsingException {
        double p1 = 1.0 / 11.0;
        double p2 = 2.0 / 11.0;
        double expectedEntropy = -(9 * p1 * (Math.log(p1) / Math.log(2.0)) +
                1 * p2 * (Math.log(p2) / Math.log(2.0)));

        assertThatMetricReportsWithin(expectedEntropy, 0.1, new TokenEntropy<>(), "./src/test/fixtures/metrics/oneMoreComplex.json");
    }
}
