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

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ClonedCodeType3Test implements JsonTest {

    @Test
    public void testDuplicatedScript() throws IOException, ParsingException {
        // 0, as clone is of type 1
        assertThatFinderReports(0, new ClonedCodeType3(), "./src/test/fixtures/smells/duplicatedScript.json");
    }

    @Test
    public void testDuplicatedScriptDifferentLiteralsAndVariables() throws IOException, ParsingException {
        // 0, as clone is of type 2
        assertThatFinderReports(0, new ClonedCodeType3(), "./src/test/fixtures/smells/codecloneliteralsvariables.json");
    }

    @Test
    public void testSubsequenceClone() throws IOException, ParsingException {
        // 0, as clone is of type 2
        assertThatFinderReports(0, new ClonedCodeType3(), "./src/test/fixtures/smells/codeclonesubsequence.json");
    }

    @Test
    public void testVariableClone() throws IOException, ParsingException {
        // 0, as clone is of type 2
        assertThatFinderReports(0, new ClonedCodeType3(), "./src/test/fixtures/smells/codeclonevariableblocks.json");
    }

    @Test
    public void testListClone() throws IOException, ParsingException {
        // 0, as clone is of type 2
        assertThatFinderReports(0, new ClonedCodeType3(), "./src/test/fixtures/smells/codeclonelistblocks.json");
    }

    @Test
    public void testCustomBlockClone() throws IOException, ParsingException {
        // 0, as clone is of type 1
        assertThatFinderReports(0, new ClonedCodeType3(), "./src/test/fixtures/smells/codeclonecustomblock.json");
    }

    @Test
    public void testCloneWithGap() throws IOException, ParsingException {
        Program program = getAST("./src/test/fixtures/smells/cloneType3WithGap.json");
        ClonedCodeType3 finder = new ClonedCodeType3();
        List<Issue> issues = new ArrayList<>(finder.check(program));
        // 0, as clone is of type 1
        assertEquals(2, issues.size());
        Issue issue1 = issues.get(0);
        Issue issue2 = issues.get(1);
        assertThat(issue1.isDuplicateOf(issue2)).isTrue();
    }


    @Test
    public void testMultipleIfCloneType123() throws IOException, ParsingException {
        Program program = getAST("./src/test/fixtures/smells/cloneType23Subsumption.json");
        ClonedCodeType2 cc2 = new ClonedCodeType2();
        ClonedCodeType3 cc3 = new ClonedCodeType3();

        List<Issue> issues2 = new ArrayList<>(cc2.check(program));
        List<Issue> issues3 = new ArrayList<>(cc3.check(program));
        assertEquals(2, issues2.size());
        assertEquals(2, issues3.size());

        assertThat(issues2.get(0).isDuplicateOf(issues3.get(0))).isFalse();
        assertThat(issues2.get(1).isDuplicateOf(issues3.get(0))).isFalse();
        assertThat(issues2.get(0).isDuplicateOf(issues3.get(1))).isFalse();
        assertThat(issues2.get(1).isDuplicateOf(issues3.get(1))).isFalse();

        assertThat(issues2.get(0).isSubsumedBy(issues3.get(0))).isFalse();
        assertThat(issues2.get(1).isSubsumedBy(issues3.get(0))).isFalse();
        assertThat(issues2.get(0).isSubsumedBy(issues3.get(1))).isFalse();
        assertThat(issues2.get(1).isSubsumedBy(issues3.get(1))).isFalse();

        assertThat(issues3.get(0).isSubsumedBy(issues2.get(0))).isTrue();
        assertThat(issues3.get(1).isSubsumedBy(issues2.get(0))).isFalse();
        assertThat(issues3.get(0).isSubsumedBy(issues2.get(1))).isFalse();
        assertThat(issues3.get(1).isSubsumedBy(issues2.get(1))).isFalse();
    }
}
