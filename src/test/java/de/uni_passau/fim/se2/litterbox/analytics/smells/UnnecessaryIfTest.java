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
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScriptReplacementVisitor;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.google.common.truth.Truth.assertThat;

public class UnnecessaryIfTest implements JsonTest {
    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        assertThatFinderReports(0, new UnnecessaryIf(), "./src/test/fixtures/emptyProject.json");
    }

    @Test
    public void testUnnecessaryIf() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/smells/unnecessaryIf.json");
        UnnecessaryIf finder = new UnnecessaryIf();
        Set<Issue> issues = finder.check(program);
        assertThat(issues).hasSize(1);

        Issue theIssue = issues.iterator().next();
        ScriptReplacementVisitor visitor = new ScriptReplacementVisitor(theIssue.getScript(), (Script) theIssue.getRefactoredScriptOrProcedureDefinition());
        Program refactoredProgram = (Program) program.accept(visitor);
        Set<Issue> refactoredIssues = finder.check(refactoredProgram);
        assertThat(refactoredIssues).isEmpty();
    }

    @Test
    public void testDoubleIf() throws IOException, ParsingException {
        assertThatFinderReports(0, new UnnecessaryIf(), "./src/test/fixtures/smells/doubleIf.json");
    }

    @Test
    public void testDifferentIf() throws IOException, ParsingException {
        assertThatFinderReports(0, new UnnecessaryIf(), "./src/test/fixtures/smells/separateIfs.json");
    }

    @Test
    public void testDuplicates() throws IOException, ParsingException {
        UnnecessaryIf unnecessaryIf = new UnnecessaryIf();
        Program program = getAST("./src/test/fixtures/smells/unnecessaryIfDuplicate.json");
        List<Issue> issues = new ArrayList<>(unnecessaryIf.check(program));
        assertThat(issues).hasSize(2);
        assertThat(issues.get(0).isDuplicateOf(issues.get(1))).isTrue();
        assertThat(issues.get(1).isDuplicateOf(issues.get(0))).isTrue();
        assertThat(issues.get(0).isDuplicateOf(issues.get(0))).isFalse();
    }
}
