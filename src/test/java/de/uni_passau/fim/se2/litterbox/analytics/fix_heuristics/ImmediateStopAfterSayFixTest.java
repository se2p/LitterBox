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
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Say;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Think;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * Tests for ImmediateStopAfterSayFix.
 * Bug: Say/Think block followed immediately by StopAll.
 * Fix: StopAll removed or intermediate block added.
 */
public class ImmediateStopAfterSayFixTest extends AbstractFixHeuristicTest {

    @Test
    public void testFixFound_NoNextStmt() throws IOException, ParsingException {
        // Fix: Say is last statement (No subsequent StopAll)
        String code = """
                when green flag clicked
                say [Hello]
                """;

        Program program = parseProgram(code);
        String blockId = findBlockId(program, Say.class);

        ImmediateStopAfterSayFix heuristic = new ImmediateStopAfterSayFix(createTestIssueDTO(blockId));
        assertFixFound(heuristic, program);
    }

    @Test
    public void testFixFound_NextStmtNotStopAll() throws IOException, ParsingException {
        // Fix: Next statement is MoveSteps (NOT StopAll)
        String code = """
                when green flag clicked
                say [Hello]
                move (10) steps
                """;

        Program program = parseProgram(code);
        String blockId = findBlockId(program, Say.class);

        ImmediateStopAfterSayFix heuristic = new ImmediateStopAfterSayFix(createTestIssueDTO(blockId));
        assertFixFound(heuristic, program);
    }

    @Test
    public void testNoFix_SayFollowedByStopAll() throws IOException, ParsingException {
        // No Fix: Next statement *IS* StopAll
        String code = """
                when green flag clicked
                say [Hello]
                stop [all v]
                """;

        Program program = parseProgram(code);
        String blockId = findBlockId(program, Say.class);

        ImmediateStopAfterSayFix heuristic = new ImmediateStopAfterSayFix(createTestIssueDTO(blockId));
        assertNoFixFound(heuristic, program);
    }

    @Test
    public void testFixFound_ThinkNoNextStmt() throws IOException, ParsingException {
        // Fix: Think is last statement
        String code = """
                when green flag clicked
                think [Hmm]
                """;

        Program program = parseProgram(code);
        String blockId = findBlockId(program, Think.class);

        ImmediateStopAfterSayFix heuristic = new ImmediateStopAfterSayFix(createTestIssueDTO(blockId));
        assertFixFound(heuristic, program);
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

        ImmediateStopAfterSayFix heuristic = new ImmediateStopAfterSayFix(createTestIssueDTO(blockId));
        assertFixFound(heuristic, program);
    }

    // ========================================================================
    // INJECTOR-BASED TESTS
    // ========================================================================

    @Test
    public void testInjector_ImmediateStopAfterSay() throws IOException, ParsingException {
        // Start with valid program (say not followed by stop)
        String fixedCode = """
                when green flag clicked
                say [Hello]
                move (10) steps
                """;

        Program fixedProgram = parseProgram(fixedCode);

        // Verify original has no bug
        de.uni_passau.fim.se2.litterbox.analytics.bugs.ImmediateStopAfterSayThink bugFinder =
                new de.uni_passau.fim.se2.litterbox.analytics.bugs.ImmediateStopAfterSayThink();
        java.util.Set<de.uni_passau.fim.se2.litterbox.analytics.Issue> originalIssues =
                bugFinder.check(fixedProgram);
        org.junit.jupiter.api.Assertions.assertTrue(originalIssues.isEmpty(),
                "Original program should be clean");

        // Inject bug (adds StopAll after Say)
        de.uni_passau.fim.se2.litterbox.injection.bugs.ImmediateStopAfterSayThinkInjector injector =
                new de.uni_passau.fim.se2.litterbox.injection.bugs.ImmediateStopAfterSayThinkInjector();
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

        // Use existing Say block ID from original program
        String existingBlockId = findBlockId(fixedProgram, Say.class);
        org.junit.jupiter.api.Assertions.assertNotNull(existingBlockId,
                "Should find Say in original program");

        ImmediateStopAfterSayFix fix = new ImmediateStopAfterSayFix(createTestIssueDTO(existingBlockId));
        assertFixFound(fix, fixedProgram);
    }
}
