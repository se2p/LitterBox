/*
 * Copyright (C) 2019-2026 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.analytics.bugs;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

public class TypeErrorDeterminismTest implements JsonTest {

    @Test
    public void testDeterminism() throws IOException, ParsingException {
        // Load the problematic project that caused the flakiness.
        // This ensures the control flow complexity (merges, loops) is sufficient to trigger specific dataflow instabilities.
        Program prog = JsonTest.parseProgram("./src/test/fixtures/bugs/flaky_project.json");

        TypeError finder = new TypeError();
        
        // Run initial check to get the baseline
        Set<Issue> baselineIssues = finder.check(prog);
        String baselineString = issuesToString(baselineIssues);

        // Run repeatedly to verify consistency. 
        // 20 iterations are sufficient to detect the non-determinism if it exists.
        for (int i = 0; i < 20; i++) {
            Set<Issue> currentIssues = finder.check(prog);
            String currentString = issuesToString(currentIssues);
            
            Assertions.assertEquals(baselineString, currentString, 
                "Iteration " + i + ": Non-deterministic issues detected. Type analysis results must be stable.");
        }
    }

    private String issuesToString(Set<Issue> issues) {
        return issues.stream()
                .map(issue -> issue.getHintText(de.uni_passau.fim.se2.litterbox.FinderTest.translator) + " @ " + issue.getCodeLocation())
                .sorted() // Sort for stable string comparison
                .collect(Collectors.joining("\n"));
    }
}
