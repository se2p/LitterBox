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
package de.uni_passau.fim.se2.litterbox.analytics.bugpattern;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

public class MissingEraseAllTest implements JsonTest {

    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        Program empty = JsonTest.parseProgram("./src/test/fixtures/emptyProject.json");
        MissingEraseAll parameterName = new MissingEraseAll();
        Set<Issue> reports = parameterName.check(empty);
        Assertions.assertTrue(reports.isEmpty());
    }

    @Test
    public void testEraseInOtherSprite() throws IOException, ParsingException {
        Program eraseOtherSprite = JsonTest.parseProgram("./src/test/fixtures/bugpattern/eraseOtherSprite.json");
        MissingEraseAll parameterName = new MissingEraseAll();
        Set<Issue> reports = parameterName.check(eraseOtherSprite);
        Assertions.assertTrue(reports.isEmpty());
    }

    @Test
    public void testMissingEraseAll() throws IOException, ParsingException {
        Program missingEraseAll = JsonTest.parseProgram("./src/test/fixtures/bugpattern/missingEraseAll.json");
        MissingEraseAll parameterName = new MissingEraseAll();
        Set<Issue> reports = parameterName.check(missingEraseAll);
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void testName() {
        MissingEraseAll parameterName = new MissingEraseAll();
        Assertions.assertEquals(parameterName.getName(), MissingEraseAll.NAME);
    }
}
