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
package de.uni_passau.fim.se2.litterbox.analytics.smells;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class UnusedVariableTest implements JsonTest {

    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        assertThatFinderReports(0, new UnusedVariable(), "./src/test/fixtures/bugpattern/recursion.json");
    }

    @Test
    public void testUnusedVariable() throws IOException, ParsingException {
        assertThatFinderReports(2, new UnusedVariable(), "./src/test/fixtures/smells/unusedVariables.json");
    }

    @Test
    public void testOneUsedVariable() throws IOException, ParsingException {
        assertThatFinderReports(2, new UnusedVariable(), "./src/test/fixtures/smells/oneUsedVariable.json");
    }

    @Test
    public void testUnusedList() throws IOException, ParsingException {
        assertThatFinderReports(1, new UnusedVariable(), "./src/test/fixtures/smells/listunused.json");
    }

    @Test
    public void testUsedList() throws IOException, ParsingException {
        assertThatFinderReports(0, new UnusedVariable(), "./src/test/fixtures/smells/listused.json");
    }
}
