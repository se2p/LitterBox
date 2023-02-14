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

public class BlockCountTest implements JsonTest {

    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        assertThatMetricReports(0, new BlockCount<>(), "./src/test/fixtures/emptyProject.json");
    }

    @Test
    public void testBlockCountNested() throws IOException, ParsingException {
        assertThatMetricReports(14, new BlockCount<>(), "./src/test/fixtures/smells/nestedLoops.json");
    }

    @Test
    public void testBlockproc() throws IOException, ParsingException {
        assertThatMetricReports(18, new BlockCount<>(), "./src/test/fixtures/blockCountWithProc.json");
    }

    @Test
    public void testFixedStatements() throws IOException, ParsingException {
        assertThatMetricReports(26, new BlockCount<>(), "./src/test/fixtures/stmtParser/allFixedStatements.json");
    }

    @Test
    public void testFixedExpr() throws IOException, ParsingException {
        assertThatMetricReports(4, new BlockCount<>(), "./src/test/fixtures/fixedExpressions.json");
    }

    @Test
    public void testTTS() throws IOException, ParsingException {
        assertThatMetricReports(3, new BlockCount<>(), "./src/test/fixtures/stmtParser/allTextToSpeech.json");
    }

    @Test
    public void testOnlyVariable() throws IOException, ParsingException {
        assertThatMetricReports(1, new BlockCount<>(), "./src/test/fixtures/onlyVariable.json");
    }

    @Test
    public void testHalfFixedExpr() throws IOException, ParsingException {
        assertThatMetricReports(5, new BlockCount<>(), "./src/test/fixtures/halfFixedExpressions.json");
        //TODO does an empty string have to be an UnspecifiedExpr?
    }

    @Test
    public void testPenAndTTS() throws IOException, ParsingException {
        assertThatMetricReports(4, new BlockCount<>(), "./src/test/fixtures/metrics/penAndTTS.json");
    }

    @Test
    public void testMusic() throws IOException, ParsingException {
        assertThatMetricReports(7, new BlockCount<>(), "./src/test/fixtures/metrics/allMusicBlocks.json");
    }

    @Test
    public void testNestedMusic() throws IOException, ParsingException {
        assertThatMetricReports(6, new BlockCount<>(), "./src/test/fixtures/metrics/nestedMusic.json");
    }

    @Test
    public void testTranslate() throws IOException, ParsingException {
        assertThatMetricReports(7, new BlockCount<>(), "./src/test/fixtures/metrics/nestedTranslate.json");
    }

    @Test
    public void testTranslater() throws IOException, ParsingException {
        assertThatMetricReports(14, new BlockCount<>(), "./src/test/fixtures/metrics/translater.json");
    }
}
