/*
 * Copyright (C) 2019-2024 LitterBox contributors
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
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class DuplicatedScriptTest implements JsonTest {

    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        assertThatFinderReports(0, new DuplicatedScript(), "./src/test/fixtures/emptyProject.json");
    }

    @Test
    public void testProgram() throws IOException, ParsingException {
        assertThatFinderReports(1, new DuplicatedScript(), "./src/test/fixtures/smells/duplicatedScript.json");
    }

    @Test
    public void testDuplicatedScriptMinimalDifference() throws IOException, ParsingException {
        // x-position and y-position sensing blocks are replaced
        assertThatFinderReports(0, new DuplicatedScript(), "./src/test/fixtures/smells/duplicatedScriptMinimalDifference.json");
    }

    @Test
    public void testDuplicatedScriptDifferentEvent() throws IOException, ParsingException {
        assertThatFinderReports(0, new DuplicatedScript(), "./src/test/fixtures/smells/duplicatedScriptDifferentEvent.json");
    }

    @Test
    public void testDuplicatedScriptMultipleBlocks() throws IOException, ParsingException {
        assertThatFinderReports(1, new DuplicatedScript(), "./src/test/fixtures/smells/duplicatedScriptMultipleBlocks.json");
    }

    @Test
    public void testDuplicatedScriptOtherSprite() throws IOException, ParsingException {
        assertThatFinderReports(0, new DuplicatedScript(), "./src/test/fixtures/smells/duplicatedScriptOtherSprite.json");
    }

    @Test
    public void testSubsumptionCovering() throws IOException, ParsingException {
        Program empty = getAST("./src/test/fixtures/smells/duplicatedScriptCovering.json");
        DuplicatedScript duplicatedScript = new DuplicatedScript();
        List<Issue> reportsDuplicatedScript = new ArrayList<>(duplicatedScript.check(empty));
        Assertions.assertEquals(1, reportsDuplicatedScript.size());
        DuplicatedScriptsCovering duplicatedScriptsCovering = new DuplicatedScriptsCovering();
        List<Issue> reportsDuplicatedScriptsCovering = new ArrayList<>(duplicatedScriptsCovering.check(empty));
        Assertions.assertEquals(1, reportsDuplicatedScriptsCovering.size());
        Assertions.assertTrue(duplicatedScript.isSubsumedBy(reportsDuplicatedScript.getFirst(), reportsDuplicatedScriptsCovering.getFirst()));
    }
}
