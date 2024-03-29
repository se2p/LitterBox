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
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.clonedetection.CloneAnalysis;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ClonedCodeType2Test implements JsonTest {

    @Test
    public void testDuplicatedScript() throws IOException, ParsingException {
        // 0, as clone is of type 1
        assertThatFinderReports(0, new ClonedCodeType2(), "./src/test/fixtures/smells/duplicatedScript.json");
    }

    @Test
    public void testDuplicatedScriptDifferentLiteralsAndVariables() throws IOException, ParsingException {
        // 0, as clone is shorter than the minSize of 6
        assertThatFinderReports(0, new ClonedCodeType2(), "./src/test/fixtures/smells/codecloneliteralsvariables.json");
    }

    @Test
    public void testSubsequenceClone() throws IOException, ParsingException {
        // 0, as clone is shorter than the minSize of 6
        assertThatFinderReports(0, new ClonedCodeType2(), "./src/test/fixtures/smells/codeclonesubsequence.json");
    }

    @Test
    public void testVariableClone() throws IOException, ParsingException {
        // 0, as clone is shorter than the minSize of 6
        assertThatFinderReports(0, new ClonedCodeType2(), "./src/test/fixtures/smells/codeclonevariableblocks.json");
    }

    @Test
    public void testListClone() throws IOException, ParsingException {
        Program program = getAST("./src/test/fixtures/smells/codeclonelistblocks.json");
        ClonedCodeType2 finder = new ClonedCodeType2();
        List<Issue> issues = new ArrayList<>(finder.check(program));
        assertEquals(2, issues.size());
        Issue issue1 = issues.get(0);
        Issue issue2 = issues.get(1);
        assertThat(issue1.isDuplicateOf(issue2)).isTrue();
    }

    @Test
    public void testCustomBlockClone() throws IOException, ParsingException {
        // 0, as clone is of type 1
        assertThatFinderReports(0, new ClonedCodeType2(), "./src/test/fixtures/smells/codeclonecustomblock.json");
    }

    @Test
    public void testCloneType1And2InOneScript() throws IOException, ParsingException {
        int origSize = CloneAnalysis.MIN_SIZE;
        Program program = getAST("./src/test/fixtures/smells/cloneType1And2.json");

        ClonedCodeType1 finder1 = new ClonedCodeType1();
        CloneAnalysis.MIN_SIZE = 3;
        List<Issue> type1Issues = new ArrayList<>(finder1.check(program));

        ClonedCodeType2 finder2 = new ClonedCodeType2();
        List<Issue> type2Issues = new ArrayList<>(finder2.check(program));
        CloneAnalysis.MIN_SIZE = origSize;

        assertThat(type1Issues).hasSize(3);
        assertThat(type2Issues).hasSize(4);

        assertThat(type1Issues.get(0).isDuplicateOf(type1Issues.get(1))).isTrue();
        assertThat(type1Issues.get(0).isDuplicateOf(type1Issues.get(2))).isTrue();
        assertThat(type1Issues.get(1).isDuplicateOf(type1Issues.get(2))).isTrue();

        assertThat(type2Issues.get(0).isDuplicateOf(type2Issues.get(0))).isFalse();
        assertThat(type2Issues.get(0).isDuplicateOf(type2Issues.get(1))).isTrue();
        assertThat(type2Issues.get(0).isDuplicateOf(type2Issues.get(2))).isTrue();
        assertThat(type2Issues.get(0).isDuplicateOf(type2Issues.get(3))).isTrue();
        assertThat(type2Issues.get(1).isDuplicateOf(type2Issues.get(2))).isTrue();
        assertThat(type2Issues.get(1).isDuplicateOf(type2Issues.get(3))).isTrue();
        assertThat(type2Issues.get(2).isDuplicateOf(type2Issues.get(3))).isTrue();
    }

    @Test
    public void testSevenInclEventOneVariableNameDifference() throws IOException, ParsingException {
        Program program = getAST("./src/test/fixtures/smells/type2CopySevenInclEvent.json");
        ClonedCodeType2 finder = new ClonedCodeType2();
        List<Issue> issues = new ArrayList<>(finder.check(program));
        assertEquals(2, issues.size());
        Issue issue1 = issues.get(0);
        Issue issue2 = issues.get(1);
        assertThat(issue1.isDuplicateOf(issue2)).isTrue();
    }
}
