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
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.DeleteClone;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * Tests for InappropriateHandlerDeleteCloneFix.
 * Bug: DeleteClone in GreenFlag script (runs on main sprite, not clone).
 * Fix: DeleteClone no longer in GreenFlag script.
 */
public class InappropriateHandlerDeleteCloneFixTest extends AbstractFixHeuristicTest {

    @Test
    public void testFixFound_DeleteCloneInCloneHandler() throws IOException, ParsingException {
        // DeleteClone in "When I start as clone" handler - fixed
        String code = """
                when I start as a clone
                move (10) steps
                delete this clone
                """;

        Program program = parseProgram(code);
        String blockId = findBlockId(program, DeleteClone.class);

        InappropriateHandlerDeleteCloneFix finder = new InappropriateHandlerDeleteCloneFix(createTestIssueDTO(blockId));
        assertFixFound(finder, program);
    }

    @Test
    public void testNoFix_DeleteCloneStillInGreenFlag() throws IOException, ParsingException {
        // DeleteClone still in GreenFlag handler - bug persists
        String code = """
                when green flag clicked
                delete this clone
                """;

        Program program = parseProgram(code);
        String blockId = findBlockId(program, DeleteClone.class);

        InappropriateHandlerDeleteCloneFix finder = new InappropriateHandlerDeleteCloneFix(createTestIssueDTO(blockId));
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

        InappropriateHandlerDeleteCloneFix finder = new InappropriateHandlerDeleteCloneFix(createTestIssueDTO(blockId));
        assertFixFound(finder, program);
    }

    // ========================================================================
    // INJECTOR-BASED TESTS
    // ========================================================================

    @Test
    public void testInjector_InappropriateHandlerDeleteClone() throws IOException, ParsingException {
        // Start with valid program (no DeleteClone in GreenFlag)
        // Has DeleteClone in clone handler which is the correct location
        String fixedCode = """
                when green flag clicked
                create clone of (myself v)
                move (10) steps
                
                when I start as a clone
                move (5) steps
                delete this clone
                """;

        Program fixedProgram = parseProgram(fixedCode);

        // Verify original has no bug
        de.uni_passau.fim.se2.litterbox.analytics.bugs.InappropriateHandlerDeleteClone bugFinder =
                new de.uni_passau.fim.se2.litterbox.analytics.bugs.InappropriateHandlerDeleteClone();
        java.util.Set<de.uni_passau.fim.se2.litterbox.analytics.Issue> originalIssues =
                bugFinder.check(fixedProgram);
        org.junit.jupiter.api.Assertions.assertTrue(originalIssues.isEmpty(),
                "Original program should be clean");

        // Inject bug (adds DeleteClone to GreenFlag script)
        de.uni_passau.fim.se2.litterbox.injection.bugs.InappropriateHandlerDeleteCloneInjector injector =
                new de.uni_passau.fim.se2.litterbox.injection.bugs.InappropriateHandlerDeleteCloneInjector();
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

        // The injector ADDS a new DeleteClone (with new block ID) to the GreenFlag script.
        // Use the DeleteClone ID from the ORIGINAL program (in clone handler) to verify
        // the fix heuristic works when the block is in the correct context.
        String existingDeleteCloneId = findBlockId(fixedProgram, DeleteClone.class);
        org.junit.jupiter.api.Assertions.assertNotNull(existingDeleteCloneId,
                "Should find DeleteClone in original program");

        InappropriateHandlerDeleteCloneFix fix = new InappropriateHandlerDeleteCloneFix(createTestIssueDTO(existingDeleteCloneId));
        assertFixFound(fix, fixedProgram);
    }
}
