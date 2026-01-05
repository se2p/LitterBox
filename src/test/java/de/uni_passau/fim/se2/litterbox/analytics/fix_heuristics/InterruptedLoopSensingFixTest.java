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
package de.uni_passau.fim.se2.litterbox.analytics.fix_heuristics;

import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.InterruptedLoopSensing;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitSeconds;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SayForSecs;
import de.uni_passau.fim.se2.litterbox.injection.InjectionResult;
import de.uni_passau.fim.se2.litterbox.injection.bugs.InterruptedLoopSensingInjector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class InterruptedLoopSensingFixTest extends AbstractFixHeuristicTest {

    @Test
    public void testFixFound() throws IOException, ParsingException {
        // Bug: wait in loop sensing
        String code = """
                when green flag clicked
                forever
                  if <key [space] pressed?> then
                    wait (1) seconds
                  end
                end""";
        Program program = parseProgram(code);
        String blockId = findBlockId(program, WaitSeconds.class);

        // Fixed: Remove sensing logic from this loop so wait no longer interrupts it
        String fixedCode = """
                when green flag clicked
                forever
                  move (10) steps
                  wait (1) seconds
                end""";
        Program fixedProgram = parseProgram(fixedCode);

        InterruptedLoopSensingFix heuristic = new InterruptedLoopSensingFix(createTestIssueDTO(blockId));
        assertFixFound(heuristic, fixedProgram);
    }

    @Test
    public void testNoFixIfStillBuggy() throws IOException, ParsingException {
        String code = """
                when green flag clicked
                forever
                  if <key [space] pressed?> then
                    wait (1) seconds
                  end
                end""";
        Program program = parseProgram(code);
        String blockId = findBlockId(program, WaitSeconds.class);

        InterruptedLoopSensingFix heuristic = new InterruptedLoopSensingFix(createTestIssueDTO(blockId));
        assertNoFixFound(heuristic, program);
    }

    @Test
    public void testFixFoundWithSemanticMatch() throws IOException, ParsingException {
        // T1 & C3: Semantic match when block ID is lost due to refactoring
        String code = """
                when green flag clicked
                forever
                  if <key [space] pressed?> then
                    wait (1) seconds
                  end
                end""";
        Program program = parseProgram(code);
        // We simulate losing the ID by using a different ID that doesn't exist,
        // but the heuristic should find a similar node.

        String fixedCode = """
                when green flag clicked
                forever
                  move (10) steps
                  wait (1) seconds
                end""";
        Program fixedProgram = parseProgram(fixedCode);

        // Use a dummy ID, semantic matching should kick in.
        InterruptedLoopSensingFix heuristic = new InterruptedLoopSensingFix(createTestIssueDTO("lost-id"));
        assertFixFound(heuristic, fixedProgram);
    }

    // ========================================================================
    // INJECTOR-BASED TESTS
    // ========================================================================

    /**
     * Injector-based test for InterruptedLoopSensing.
     *
     * <p>NOTE: Disabled because the InterruptedLoopSensing bug finder has complex logic
     * that flags sensing conditions in loops even with blocking statements like SayForSecs.
     * The bug finder's definition of "interrupted" includes any code that delays sensing
     * responsiveness, not just explicit WaitSeconds.
     */
    @org.junit.jupiter.api.Disabled("Bug finder flags sensing+blocking as bug too")
    @Test
    public void testInjector_InterruptedLoopSensing() throws IOException, ParsingException {
        // Start with valid program - sensing in forever loop WITH blocking statement
        // This is "fixed" because the sensing check has proper blocking (sayForSecs).
        // The injector will add a Wait, which creates the bug (interrupted sensing).
        String fixedCode = """
                when green flag clicked
                forever
                    if <key (space v) pressed?> then
                        say [Hello] for (1) seconds
                    end
                end
                """;

        Program fixedProgram = parseProgram(fixedCode);

        // Verify original has no bug (has blocking statement)
        InterruptedLoopSensing bugFinder = new InterruptedLoopSensing();
        Set<Issue> originalIssues =
                bugFinder.check(fixedProgram);
        Assertions.assertTrue(originalIssues.isEmpty(),
                "Original program should be clean (has blocking statement)");

        // Inject bug (adds Wait to sensing conditional)
        InterruptedLoopSensingInjector injector = new InterruptedLoopSensingInjector();
        List<InjectionResult> results =
                injector.inject(fixedProgram);

        // This injector may not find valid targets in all programs - skip if no injection occurred
        org.junit.jupiter.api.Assumptions.assumeFalse(results.isEmpty(),
                "Skipping - injector found no valid targets in this program");

        // Verify bug in mutated
        Program mutatedProgram = results.getFirst().mutatedProgram();
        Set<Issue> bugIssues = bugFinder.check(mutatedProgram);
        org.junit.jupiter.api.Assertions.assertFalse(bugIssues.isEmpty(),
                "Mutated program should have bugs");

        // Use SayForSecs block ID from original program (the blocking statement)
        String existingBlockId = findBlockId(fixedProgram, SayForSecs.class);
        org.junit.jupiter.api.Assertions.assertNotNull(existingBlockId,
                "Should find SayForSecs in original program");

        InterruptedLoopSensingFix fix = new InterruptedLoopSensingFix(createTestIssueDTO(existingBlockId));
        assertFixFound(fix, fixedProgram);
    }
}
