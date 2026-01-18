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
package de.uni_passau.fim.se2.litterbox.injection.bugs;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.MissingLoopSensing;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.export.scratchblocks.ScratchBlocksVisitor;
import de.uni_passau.fim.se2.litterbox.injection.InjectionResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class MissingLoopSensingInjectorTest implements JsonTest {

    @Test
    public void testMissingLoopSensingInjector() throws IOException, ParsingException {
        // Loop containing an if-statement with sensing condition - no bug
        String script = """
                when green flag clicked
                forever
                    if <key [space] pressed?> then
                        move (10) steps
                    end
                end
                """;

        Program program = parseScratchBlocks(script);

        // Verify original program has no issue
        MissingLoopSensing detector = new MissingLoopSensing();
        Set<Issue> originalIssues = detector.check(program);
        Assertions.assertTrue(originalIssues.isEmpty(), "Original program should have no issue");

        // Inject bug
        MissingLoopSensingInjector injector = new MissingLoopSensingInjector();
        List<InjectionResult> injected = injector.inject(program);

        Assertions.assertFalse(injected.isEmpty(), "Should inject bug");
        Assertions.assertEquals(1, injected.size(), "Should inject exactly 1 bug program");
        Program mutated = injected.getFirst().mutatedProgram();

        // Verification: Detector should find the bug after the loop is removed
        Set<Issue> issues = detector.check(mutated);
        Assertions.assertFalse(issues.isEmpty(), "Detector should find Missing Loop Sensing issue");
    }

    @Test
    public void testMultipleIfConditionsInLoop() throws IOException, ParsingException {
        // Loop containing TWO if-statements with sensing conditions
        String script = """
                when green flag clicked
                forever
                    if <key [space] pressed?> then
                        move (10) steps
                    end
                    if <touching [edge v] ?> then
                        turn right (180) degrees
                    end
                end
                """;

        Program program = parseScratchBlocks(script);

        // Verify original program has no issue
        MissingLoopSensing detector = new MissingLoopSensing();
        Set<Issue> originalIssues = detector.check(program);
        Assertions.assertTrue(originalIssues.isEmpty(), "Original program should have no issue");

        // Inject bug
        MissingLoopSensingInjector injector = new MissingLoopSensingInjector();
        List<InjectionResult> injected = injector.inject(program);

        // Should still inject 1 program (one mutation per loop)
        Assertions.assertEquals(1, injected.size(), "Should inject exactly 1 bug program");
        Program mutated = injected.getFirst().mutatedProgram();

        // Verify scratchblocks output - ALL body statements should remain, loop is removed
        String result = ScratchBlocksVisitor.of(mutated);
        String expectedScript = """
                when green flag clicked
                if <key [space] pressed?> then
                move (10) steps
                end
                if <touching \\[edge v\\] ?> then
                turn right (180) degrees
                end
                """;
        Assertions.assertTrue(result.replace(System.lineSeparator(), "\n").contains(expectedScript.trim()),
                "Should have ALL body statements, with loop removed. Got:\n" + result);

        // Verification: Detector should find the bug
        Set<Issue> issues = detector.check(mutated);
        Assertions.assertFalse(issues.isEmpty(), "Detector should find Missing Loop Sensing issue");
    }
}
