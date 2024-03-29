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
import de.uni_passau.fim.se2.litterbox.analytics.bugpattern.ForeverInsideLoop;
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

public class UnnecessaryLoopTest implements JsonTest {
    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        assertThatFinderReports(0, new UnnecessaryLoop(), "./src/test/fixtures/emptyProject.json");
    }

    @Test
    public void testUnnecessary() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/smells/loopOne.json");
        UnnecessaryLoop finder = new UnnecessaryLoop();
        Set<Issue> issues = finder.check(program);
        assertThat(issues).hasSize(2);
        for (Issue issue : issues) {
            ScriptReplacementVisitor visitor = new ScriptReplacementVisitor(issue.getScript(), (Script) issue.getRefactoredScriptOrProcedureDefinition());
            Program refactoredProgram = (Program) program.accept(visitor);
            Set<Issue> refactoredIssues = finder.check(refactoredProgram);
            assertThat(refactoredIssues).hasSize(1);
        }
    }

    @Test
    public void testUnnecessarySubsumption() throws IOException, ParsingException {
        Program longScript = getAST("./src/test/fixtures/bugpattern/unnecessaryForever.json");
        UnnecessaryLoop parameterName = new UnnecessaryLoop();
        List<Issue> reports = new ArrayList<>(parameterName.check(longScript));
        Assertions.assertEquals(1, reports.size());
        ForeverInsideLoop foreverInsideLoop = new ForeverInsideLoop();
        List<Issue> reportsForever = new ArrayList<>(foreverInsideLoop.check(longScript));
        Assertions.assertEquals(1, reportsForever.size());
        Assertions.assertTrue(reports.get(0).isSubsumedBy(reportsForever.get(0)));
    }
}
