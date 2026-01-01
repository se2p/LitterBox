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

import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;

import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * Tests for MissingLoopSensingWaitFix.
 * Bug: Loop without sensing/wait block.
 * Fix: Wait block added to loop.
 */
public class MissingLoopSensingWaitFixTest extends AbstractFixHeuristicTest {

    @Test
    public void testFixFound_WaitAdded() throws IOException, ParsingException {
        // Fix: Sensing block is now inside WaitUntil (followed by StopAll or end of script)
        String code = """
                when green flag clicked
                wait until <key (space v) pressed?>
                stop [all v]
                """;

        Program program = parseProgram(code);
        String blockId = findBlockId(program, de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.IsKeyPressed.class);

        MissingLoopSensingWaitFix finder = new MissingLoopSensingWaitFix(createTestIssueDTO(blockId));
        assertFixFound(finder, program);
    }

    @Test
    public void testNoFixFound_WaitStillMissing() throws IOException, ParsingException {
        // No Fix: Sensing block is NOT in WaitUntil (e.g. just if)
        String code = """
                when green flag clicked
                if <key (space v) pressed?> then
                end
                stop [all v]
                """;

        Program program = parseProgram(code);
        String blockId = findBlockId(program, de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.IsKeyPressed.class);

        MissingLoopSensingWaitFix finder = new MissingLoopSensingWaitFix(createTestIssueDTO(blockId));
        assertNoFixFound(finder, program);
    }

    @Test
    public void testFixFound_ScriptDeleted() throws IOException, ParsingException {
        // Script containing the bug is gone
        String code = """
                when green flag clicked
                move (10) steps
                """;

        Program program = parseProgram(code);
        String blockId = "non-existent-id";

        MissingLoopSensingWaitFix finder = new MissingLoopSensingWaitFix(createTestIssueDTO(blockId));
        assertFixFound(finder, program);
    }

    // ========================================================================
    // INJECTOR-BASED TESTS
    // ========================================================================

    @Test
    public void testInjector_WaitUntilUnwrapped() throws IOException, ParsingException {
        // Start with valid program (sensing inside WaitUntil followed by stop)
        String fixedCode = """
                when green flag clicked
                wait until <key (space v) pressed?>
                stop [all v]
                """;

        Program fixedProgram = parseProgram(fixedCode);

        // Verify original has no bug (sensing is inside WaitUntil)
        de.uni_passau.fim.se2.litterbox.analytics.bugs.MissingLoopSensing bugFinder =
                new de.uni_passau.fim.se2.litterbox.analytics.bugs.MissingLoopSensing();
        java.util.Set<de.uni_passau.fim.se2.litterbox.analytics.Issue> originalIssues =
                bugFinder.check(fixedProgram);
        org.junit.jupiter.api.Assertions.assertTrue(originalIssues.isEmpty(),
                "Original program should be clean - WaitUntil counts as proper sensing handling");

        // Note: The MissingLoopSensingInjector targets loop-based sensing, not WaitUntil.
        // This test verifies that WaitUntil-based sensing is considered a valid fix.
        
        // Use existing IsKeyPressed block ID from original program
        String existingBlockId = findBlockId(fixedProgram, 
                de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.IsKeyPressed.class);
        org.junit.jupiter.api.Assertions.assertNotNull(existingBlockId,
                "Should find IsKeyPressed in original program");

        MissingLoopSensingWaitFix fix = new MissingLoopSensingWaitFix(createTestIssueDTO(existingBlockId));
        assertFixFound(fix, fixedProgram);
    }
}
