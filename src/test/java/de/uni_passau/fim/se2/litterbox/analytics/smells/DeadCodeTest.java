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

public class DeadCodeTest implements JsonTest {

    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        assertThatFinderReports(0, new DeadCode(), "./src/test/fixtures/emptyProject.json");
    }

    @Test
    public void testDeadCode() throws IOException, ParsingException {
        assertThatFinderReports(3, new DeadCode(), "./src/test/fixtures/smells/deadCode.json");
    }

    @Test
    public void testDeadVariable() throws IOException, ParsingException {
        assertThatFinderReports(2, new DeadCode(), "./src/test/fixtures/smells/deadVariable.json");
    }

    @Test
    public void testDeadParam() throws IOException, ParsingException {
        assertThatFinderReports(2, new DeadCode(), "./src/test/fixtures/smells/deadParam.json");
    }

    @Test
    public void testAllDead() throws IOException, ParsingException {
        assertThatFinderReports(126, new DeadCode(), "./src/test/fixtures/smells/allBlocksDead.json");
    }
}
