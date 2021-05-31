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
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.google.common.truth.Truth.assertThat;

public class HalsteadMetricsTest implements JsonTest {

    @Test
    public void testLength() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/cfg/ifelse_repeattimes.json");
        HalsteadLength halsteadLength = new HalsteadLength();
        double length = halsteadLength.calculateMetric(program);
        assertThat(length).isEqualTo(12);
    }

    @Test
    public void testSize() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/cfg/ifelse_repeattimes.json");
        HalsteadVocabulary halsteadVocabulary = new HalsteadVocabulary();
        double size = halsteadVocabulary.calculateMetric(program);
        assertThat(size).isEqualTo(7);
    }

    @Test
    public void testVolume() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/cfg/ifelse_repeattimes.json");
        HalsteadVolume halsteadVolume = new HalsteadVolume();
        double volume = halsteadVolume.calculateMetric(program);

        // V = N * log2(n)
        assertThat(volume).isWithin(0.1).of(33.7);
    }

    @Test
    public void testDifficulty() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/cfg/ifelse_repeattimes.json");
        HalsteadDifficulty halsteadDifficulty = new HalsteadDifficulty();
        double difficulty = halsteadDifficulty.calculateMetric(program);

        //  D = ( n1 / 2 ) * ( N2 / n2 )
        assertThat(difficulty).isEqualTo(6.25);
    }

    @Test
    public void testEffort() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/cfg/ifelse_repeattimes.json");
        HalsteadEffort halsteadEffort = new HalsteadEffort();
        double effort = halsteadEffort.calculateMetric(program);

        // E = V * D
        assertThat(effort).isWithin(0.1).of(210.6);
    }

    @Test
    public void testNoDivisionByZeroWithoutOperands_Effort() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/metrics/halstead_bug.json");
        HalsteadEffort halsteadEffort = new HalsteadEffort();
        double difficulty = halsteadEffort.calculateMetric(program);
        assertThat(difficulty).isEqualTo(0);
    }

    @Test
    public void testNoDivisionByZeroWithoutOperands_Difficulty() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/metrics/halstead_bug.json");
        HalsteadDifficulty halsteadDifficulty = new HalsteadDifficulty();
        double difficulty = halsteadDifficulty.calculateMetric(program);
        assertThat(difficulty).isEqualTo(0);
    }

    @Test
    public void testVolume_EmptyProject() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/metrics/empty.json");
        HalsteadVolume halsteadVolume = new HalsteadVolume();
        double volume = halsteadVolume.calculateMetric(program);
        assertThat(volume).isEqualTo(0);
    }

}
