/*
 * Copyright (C) 2019-2025 LitterBox contributors
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
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.IsKeyPressed;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * Tests for MissingLoopSensingLoopFix.
 * Bug: Sensing block not inside a loop.
 * Fix: Sensing block is now inside a loop.
 */
public class MissingLoopSensingLoopFixTest extends AbstractFixHeuristicTest {

    @Test
    public void testFixFound_LoopAdded() throws IOException, ParsingException {
        // Sensing block now inside loop - fixed
        String code = """
                when green flag clicked
                forever
                    if <key (space v) pressed?> then
                        move (10) steps
                    end
                end
                """;

        Program program = parseProgram(code);
        String blockId = findBlockId(program, IsKeyPressed.class);

        MissingLoopSensingLoopFix finder = new MissingLoopSensingLoopFix(createTestIssueDTO(blockId));
        assertFixFound(finder, program);
    }

    @Test
    public void testNoFixFound_LoopStillMissing() throws IOException, ParsingException {
        // Sensing block still outside loop
        String code = """
                when green flag clicked
                if <key (space v) pressed?> then
                    move (10) steps
                end
                """;

        Program program = parseProgram(code);
        String blockId = findBlockId(program, IsKeyPressed.class);

        MissingLoopSensingLoopFix finder = new MissingLoopSensingLoopFix(createTestIssueDTO(blockId));
        assertNoFixFound(finder, program);
    }

    @Test
    public void testFixFound_ScriptDeleted() throws IOException, ParsingException {
        // Script containing sensing block is gone
        String code = """
                when green flag clicked
                move (10) steps
                """;

        Program program = parseProgram(code);
        String blockId = "non-existent-id";

        MissingLoopSensingLoopFix finder = new MissingLoopSensingLoopFix(createTestIssueDTO(blockId));
        assertFixFound(finder, program);
    }

    // ========================================================================
    // INJECTOR-BASED TESTS
    // ========================================================================

    @Test
    public void testInjector_LoopUnwrapped() throws IOException, ParsingException {
        // Start with valid program (sensing inside a loop)
        String fixedCode = """
                when green flag clicked
                forever
                    if <key (space v) pressed?> then
                        move (10) steps
                    end
                end
                """;

        Program fixedProgram = parseProgram(fixedCode);

        // Verify original has no bug (sensing is inside loop)
        de.uni_passau.fim.se2.litterbox.analytics.bugs.MissingLoopSensing bugFinder =
                new de.uni_passau.fim.se2.litterbox.analytics.bugs.MissingLoopSensing();
        java.util.Set<de.uni_passau.fim.se2.litterbox.analytics.Issue> originalIssues =
                bugFinder.check(fixedProgram);
        org.junit.jupiter.api.Assertions.assertTrue(originalIssues.isEmpty(),
                "Original program should be clean");

        // Inject bug (unwraps loop, sensing is now outside)
        de.uni_passau.fim.se2.litterbox.injection.bugs.MissingLoopSensingInjector injector =
                new de.uni_passau.fim.se2.litterbox.injection.bugs.MissingLoopSensingInjector();
        java.util.List<de.uni_passau.fim.se2.litterbox.injection.InjectionResult> results =
                injector.inject(fixedProgram);
        org.junit.jupiter.api.Assertions.assertFalse(results.isEmpty(),
                "Injector should produce results");

        // Verify bug in mutated
        Program mutatedProgram = results.get(0).mutatedProgram();
        java.util.Set<de.uni_passau.fim.se2.litterbox.analytics.Issue> bugIssues =
                bugFinder.check(mutatedProgram);
        org.junit.jupiter.api.Assertions.assertFalse(bugIssues.isEmpty(),
                "Mutated program should have bugs");

        // Use existing IsKeyPressed block ID from original program
        String existingBlockId = findBlockId(fixedProgram, IsKeyPressed.class);
        org.junit.jupiter.api.Assertions.assertNotNull(existingBlockId,
                "Should find IsKeyPressed in original program");

        MissingLoopSensingLoopFix fix = new MissingLoopSensingLoopFix(createTestIssueDTO(existingBlockId));
        assertFixFound(fix, fixedProgram);
    }
}
