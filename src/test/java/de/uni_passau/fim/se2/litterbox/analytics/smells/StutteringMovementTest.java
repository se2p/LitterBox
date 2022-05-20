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
import de.uni_passau.fim.se2.litterbox.analytics.smells.StutteringMovement;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScriptReplacementVisitor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.google.common.truth.Truth.assertThat;

public class StutteringMovementTest implements JsonTest {

    @Test
    public void testStutteringMovement() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/bugpattern/stutteringMovement.json");
        StutteringMovement finder = new StutteringMovement();
        Set<Issue> issues = finder.check(program);
        assertThat(issues).hasSize(5);

        for (Issue theIssue : issues) {
            ScriptReplacementVisitor visitor = new ScriptReplacementVisitor(theIssue.getScript(), (Script) theIssue.getRefactoredScriptOrProcedureDefinition());
            Program refactoredProgram = (Program) program.accept(visitor);
            Set<Issue> refactoredIssues = finder.check(refactoredProgram);
            assertThat(refactoredIssues).hasSize(4);
        }
    }

    @Test
    public void testDeleteParam() throws IOException, ParsingException {
        assertThatFinderReports(0, new StutteringMovement(), "./src/test/fixtures/stmtParser/deleteParam.json");
    }

    @Test
    public void testStutteringRotation() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/bugpattern/stutteringRotation.json");
        StutteringMovement finder = new StutteringMovement();
        Set<Issue> issues = finder.check(program);
        assertThat(issues).hasSize(2);

        for (Issue theIssue : issues) {
            ScriptReplacementVisitor visitor = new ScriptReplacementVisitor(theIssue.getScript(), (Script) theIssue.getRefactoredScriptOrProcedureDefinition());
            Program refactoredProgram = (Program) program.accept(visitor);
            Set<Issue> refactoredIssues = finder.check(refactoredProgram);
            assertThat(refactoredIssues).hasSize(1);
        }
    }

    @Test
    public void testStutteringMovementReset() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/bugpattern/stutteringMovementBug.json");
        StutteringMovement finder = new StutteringMovement();
        Set<Issue> issues = finder.check(program);
        assertThat(issues).hasSize(1);

        Issue theIssue = issues.iterator().next();
        ScriptReplacementVisitor visitor = new ScriptReplacementVisitor(theIssue.getScript(), (Script) theIssue.getRefactoredScriptOrProcedureDefinition());
        Program refactoredProgram = (Program) program.accept(visitor);
        Set<Issue> refactoredIssues = finder.check(refactoredProgram);
        assertThat(refactoredIssues).isEmpty();
    }

    @Test
    public void testStutteringMovementDuplicates() throws IOException, ParsingException {
        Program stutteringMovement = getAST("./src/test/fixtures/bugpattern/doubleStuttering.json");
        StutteringMovement finder = new StutteringMovement();
        List<Issue> reports = new ArrayList<>(finder.check(stutteringMovement));
        Assertions.assertEquals(2, reports.size());
        Assertions.assertTrue(reports.get(0).isDuplicateOf(reports.get(1)));
    }

    @Test
    public void testNonStuttering() throws IOException, ParsingException {
        assertThatFinderReports(0, new StutteringMovement(), "./src/test/fixtures/bugpattern/nonStuttering.json");
    }
}
