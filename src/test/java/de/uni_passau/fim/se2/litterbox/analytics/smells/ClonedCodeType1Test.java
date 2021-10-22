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
package de.uni_passau.fim.se2.litterbox.analytics.smells;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ClonedCodeType1Test implements JsonTest {

    @Test
    public void testDuplicatedScript() throws IOException, ParsingException {
        assertThatFinderReports(2, new ClonedCodeType1(), "./src/test/fixtures/smells/duplicatedScript.json");
    }

    @Test
    public void testDuplicatedScriptDifferentLiteralsAndVariables() throws IOException, ParsingException {
        // 0, as clone is of type 2
        assertThatFinderReports(0, new ClonedCodeType1(), "./src/test/fixtures/smells/codecloneliteralsvariables.json");
    }

    @Test
    public void testSubsequenceClone() throws IOException, ParsingException {
        // 0, as clone is of type 2
        assertThatFinderReports(0, new ClonedCodeType1(), "./src/test/fixtures/smells/codeclonesubsequence.json");
    }

    @Test
    public void testVariableClone() throws IOException, ParsingException {
        // 0, as clone is of type 2
        assertThatFinderReports(0, new ClonedCodeType1(), "./src/test/fixtures/smells/codeclonevariableblocks.json");
    }

    @Test
    public void testCompareScriptWithItself() throws IOException, ParsingException {
        assertThatFinderReports(2, new ClonedCodeType1(), "./src/test/fixtures/smells/cloneScriptWithItself.json");
    }

    @Test
    public void testListClone() throws IOException, ParsingException {
        // 0, as clone is of type 2
        assertThatFinderReports(0, new ClonedCodeType1(), "./src/test/fixtures/smells/codeclonelistblocks.json");
    }

    @Test
    public void testCustomBlockClone() throws IOException, ParsingException {
        // 0, as clone is shorter than the minSize of 6
        assertThatFinderReports(0, new ClonedCodeType1(), "./src/test/fixtures/smells/codeclonecustomblock.json");
    }

    @Test
    public void testDuplicatedScriptSixInclEvent() throws IOException, ParsingException {
        //0, event is not counted
        assertThatFinderReports(0, new ClonedCodeType1(), "./src/test/fixtures/smells/exactCopySixInclEvent.json");
    }

    @Test
    public void testDuplicatedScriptSevenInclEvent() throws IOException, ParsingException {
        Program program = getAST("./src/test/fixtures/smells/exactCopySevenInclEvent.json");
        ClonedCodeType1 finder = new ClonedCodeType1();
        List<Issue> issues = new ArrayList<>(finder.check(program));
        assertEquals(2, issues.size());
        assertTrue(issues.get(0).isDuplicateOf(issues.get(1)));
    }
}
