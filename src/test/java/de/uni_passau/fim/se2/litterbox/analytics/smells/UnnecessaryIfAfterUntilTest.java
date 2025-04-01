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
import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.bugpattern.MissingLoopSensing;
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

public class UnnecessaryIfAfterUntilTest implements JsonTest {
    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        assertThatFinderReports(0, new UnnecessaryIfAfterUntil(), "src/test/fixtures/emptyProject.json");
    }

    @Test
    public void testIfThenUnnecessary() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/smells/unnecessaryIfAfterUntil.json");
        UnnecessaryIfAfterUntil finder = new UnnecessaryIfAfterUntil();
        Set<Issue> reports = finder.check(program);
        assertThat(reports).hasSize(1);

        Issue theIssue = reports.iterator().next();
        Hint expectedHint = Hint.fromKey(finder.getName());
        assertThat(theIssue.getHintText()).isEqualTo(expectedHint.getHintText());

        ScriptReplacementVisitor visitor = new ScriptReplacementVisitor(theIssue.getScript(), (Script) theIssue.getRefactoredScriptOrProcedureDefinition());
        Program refactoredProgram = (Program) program.accept(visitor);
        Set<Issue> refactoredIssues = finder.check(refactoredProgram);
        assertThat(refactoredIssues).isEmpty();
    }

    @Test
    public void testCoupling() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/smells/unnecessaryIfAfterUntil.json");
        UnnecessaryIfAfterUntil finder = new UnnecessaryIfAfterUntil();
        List<Issue> reportsUnnecessaryIf = new ArrayList<>(finder.check(program));
        Assertions.assertEquals(1, reportsUnnecessaryIf.size());
        MissingLoopSensing mls = new MissingLoopSensing();
        List<Issue> reportsMLS = new ArrayList<>(mls.check(program));
        Assertions.assertEquals(1, reportsMLS.size());
        Assertions.assertTrue(finder.areCoupled(reportsUnnecessaryIf.get(0), reportsMLS.get(0)));
        Assertions.assertTrue(mls.areCoupled(reportsMLS.get(0), reportsUnnecessaryIf.get(0)));
    }
}
