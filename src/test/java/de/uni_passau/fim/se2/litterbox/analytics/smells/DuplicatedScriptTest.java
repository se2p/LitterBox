/*
 * Copyright (C) 2020 LitterBox contributors
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
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

class DuplicatedScriptTest implements JsonTest {

    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        Program empty = JsonTest.parseProgram("./src/test/fixtures/emptyProject.json");
        DuplicatedScript parameterName = new DuplicatedScript();
        Set<Issue> reports = parameterName.check(empty);
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testProgram() throws IOException, ParsingException {
        Program duplicatedScript = JsonTest.parseProgram("./src/test/fixtures/smells/duplicatedScript.json");
        DuplicatedScript finder = new DuplicatedScript();
        Set<Issue> reports = finder.check(duplicatedScript);
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void testDuplicatedScriptMinimalDifference() throws IOException, ParsingException {
        Program duplicatedScriptMinimalDifference = JsonTest.parseProgram("./src/test/fixtures/smells/duplicatedScriptMinimalDifference.json");
        DuplicatedScript finder = new DuplicatedScript();
        Set<Issue> reports = finder.check(duplicatedScriptMinimalDifference);
        // x-position and y-position sensing blocks are replaced
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testDuplicatedScriptDifferentEvent() throws IOException, ParsingException {
        Program duplicatedScriptDifferentEvent = JsonTest.parseProgram("./src/test/fixtures/smells/duplicatedScriptDifferentEvent.json");
        DuplicatedScript finder = new DuplicatedScript();
        Set<Issue> reports = finder.check(duplicatedScriptDifferentEvent);
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testDuplicatedScriptMultipleBlocks() throws IOException, ParsingException {
        Program duplicatedScriptMultipleBlocks = JsonTest.parseProgram("./src/test/fixtures/smells/duplicatedScriptMultipleBlocks.json");
        DuplicatedScript finder = new DuplicatedScript();
        Set<Issue> reports = finder.check(duplicatedScriptMultipleBlocks);
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void testDuplicatedScriptOtherSprite() throws IOException, ParsingException {
        Program duplicatedScriptOtherSprite = JsonTest.parseProgram("./src/test/fixtures/smells/duplicatedScriptOtherSprite.json");
        DuplicatedScript finder = new DuplicatedScript();
        Set<Issue> reports = finder.check(duplicatedScriptOtherSprite);
        Assertions.assertEquals(0, reports.size());
    }
}
