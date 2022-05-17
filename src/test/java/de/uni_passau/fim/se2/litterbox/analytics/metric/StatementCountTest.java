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

public class StatementCountTest implements JsonTest {
    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        assertThatMetricReports(0, new StatementCount<>(), "./src/test/fixtures/emptyProject.json");
    }

    @Test
    public void testProcedures() throws IOException, ParsingException {
        assertThatMetricReports(3, new StatementCount<>(), "./src/test/fixtures/smells/unusedEmptyProcedure.json");
    }

    @Test
    public void testNonEmptyScripts() throws IOException, ParsingException {
        assertThatMetricReports(6, new StatementCount<>(), "./src/test/fixtures/weightedMethod.json");
    }

    @Test
    public void testLooseScript() throws IOException, ParsingException {
        assertThatMetricReports(3, new StatementCount<>(), "./src/test/fixtures/metrics/looseAndNoneLooseScript.json");
    }

    @Test
    public void testDeadCode() throws IOException, ParsingException {
        assertThatMetricReports(5, new StatementCount<>(), "./src/test/fixtures/smells/deadCode.json");
    }

    @Test
    public void testPenAndTTS() throws IOException, ParsingException {
        assertThatMetricReports(4, new StatementCount<>(), "./src/test/fixtures/metrics/penAndTTS.json");
    }
}
