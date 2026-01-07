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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.ImmediateDeleteCloneAfterBroadcast;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.Broadcast;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.BroadcastAndWait;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.injection.InjectionResult;
import de.uni_passau.fim.se2.litterbox.injection.bugs.ImmediateDeleteCloneAfterBroadcastInjector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class ImmediateDeleteCloneAfterBroadcastFixTest extends AbstractFixHeuristicTest {

    @Test
    public void testFixFoundNoNextStmt() throws IOException, ParsingException {
        // Fix: Broadcast is last statement (no subsequent DeleteClone)
        String source = """
                when I start as a clone
                broadcast (hello v)
                """;

        Program program = parseProgram(source);
        String bugLocationBlockId = findBlockId(program, Broadcast.class);

        ImmediateDeleteCloneAfterBroadcastFix heuristic = new ImmediateDeleteCloneAfterBroadcastFix(createTestIssueDTO(bugLocationBlockId));
        assertFixFound(heuristic, program);
    }

    @Test
    public void testFixFoundNextStmtNotDeleteClone() throws IOException, ParsingException {
        // Fix: Next statement is MoveSteps (NOT DeleteClone)
        String source = """
                when I start as a clone
                broadcast (hello v)
                move (10) steps
                """;

        Program program = parseProgram(source);
        String bugLocationBlockId = findBlockId(program, Broadcast.class);

        ImmediateDeleteCloneAfterBroadcastFix heuristic = new ImmediateDeleteCloneAfterBroadcastFix(createTestIssueDTO(bugLocationBlockId));
        assertFixFound(heuristic, program);
    }

    @Test
    public void testNoFixBroadcastFollowedByDeleteClone() throws IOException, ParsingException {
        // No Fix: Next statement *IS* DeleteClone
        String source = """
                when I start as a clone
                broadcast (hello v)
                delete this clone
                """;

        Program program = parseProgram(source);
        String bugLocationBlockId = findBlockId(program, Broadcast.class);

        ImmediateDeleteCloneAfterBroadcastFix heuristic = new ImmediateDeleteCloneAfterBroadcastFix(createTestIssueDTO(bugLocationBlockId));
        assertNoFixFound(heuristic, program);
    }

    @Test
    public void testFixFoundBroadcastAndWait() throws IOException, ParsingException {
        // Fix: Changed to BroadcastAndWait - using parentheses syntax
        String source = """
                when I start as a clone
                broadcast (hello v) and wait
                """;

        Program program = parseProgram(source);
        String bugLocationBlockId = findBlockId(program, BroadcastAndWait.class);

        ImmediateDeleteCloneAfterBroadcastFix heuristic = new ImmediateDeleteCloneAfterBroadcastFix(createTestIssueDTO(bugLocationBlockId));
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

        ImmediateDeleteCloneAfterBroadcastFix heuristic = new ImmediateDeleteCloneAfterBroadcastFix(createTestIssueDTO(blockId));
        assertFixFound(heuristic, program);
    }

    @Test
    public void testFix_MultipleNonYieldingStmts() throws IOException, ParsingException {
        // Fix: Multiple non-yielding statements between Broadcast and DeleteClone breaks the "Immediate" condition
        String source = """
                when I start as a clone
                broadcast (hello v)
                move (10) steps
                change [myVar v] by (1)
                delete this clone
                """;

        Program program = parseProgram(source);
        String bugLocationBlockId = findBlockId(program, Broadcast.class);

        ImmediateDeleteCloneAfterBroadcastFix heuristic = new ImmediateDeleteCloneAfterBroadcastFix(createTestIssueDTO(bugLocationBlockId));
        assertFixFound(heuristic, program);
    }

    @Test
    public void testFixWithWait() throws IOException, ParsingException {
        // Fix: Yielding statement (Wait) between Broadcast and DeleteClone
        String source = """
                when I start as a clone
                broadcast (hello v)
                wait (1) secs
                delete this clone
                """;

        Program program = parseProgram(source);
        String bugLocationBlockId = findBlockId(program, Broadcast.class);

        ImmediateDeleteCloneAfterBroadcastFix heuristic = new ImmediateDeleteCloneAfterBroadcastFix(createTestIssueDTO(bugLocationBlockId));
        assertFixFound(heuristic, program);
    }

    // ========================================================================
    // INJECTOR-BASED TESTS
    // ========================================================================

    @Test
    public void testInjector_ImmediateDeleteCloneAfterBroadcast() throws IOException, ParsingException {
        // Start with valid program (broadcast NOT followed by delete clone)
        String fixedCode = """
                when green flag clicked
                create clone of (myself v)

                when I start as a clone
                broadcast (hello v)
                wait (1) secs
                move (10) steps
                """;

        Program fixedProgram = parseProgram(fixedCode);

        // Verify original has no bug
        ImmediateDeleteCloneAfterBroadcast bugFinder = new ImmediateDeleteCloneAfterBroadcast();
        Set<Issue> originalIssues = bugFinder.check(fixedProgram);
        Assertions.assertTrue(originalIssues.isEmpty(),
                "Original program should be clean");

        // Inject bug (adds DeleteClone after Broadcast)
        ImmediateDeleteCloneAfterBroadcastInjector injector =
                new ImmediateDeleteCloneAfterBroadcastInjector();
        List<InjectionResult> results = injector.inject(fixedProgram);
        Assertions.assertFalse(results.isEmpty(),
                "Injector should produce results");

        // Verify bug in mutated
        Program mutatedProgram = results.getFirst().mutatedProgram();
        Set<Issue> bugIssues = bugFinder.check(mutatedProgram);
        Assertions.assertFalse(bugIssues.isEmpty(),
                "Mutated program should have bugs");

        // Fix check: Get bug location and verify fix heuristic identifies original as fixed
        String bugBlockId = AstNodeUtil.getBlockId(bugIssues.iterator().next().getCodeLocation());

        ImmediateDeleteCloneAfterBroadcastFix fix = new ImmediateDeleteCloneAfterBroadcastFix(createTestIssueDTO(bugBlockId));
        assertFixFound(fix, fixedProgram);
    }
}
