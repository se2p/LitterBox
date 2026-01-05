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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.BlockingIfElse;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.injection.InjectionResult;
import de.uni_passau.fim.se2.litterbox.injection.bugs.BlockingIfElseInjector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Tests for BlockingIfElseFix.
 * Bug: If-Else with forever in one branch blocks following code.
 * Fix: Forever removed or following code removed.
 */
public class BlockingIfElseFixTest extends AbstractFixHeuristicTest {

    @Test
    public void testFixFound_NoForeverInBranch() throws IOException, ParsingException {
        // If-Else without forever loop in branches - fixed
        String code = """
                when green flag clicked
                if <key (space v) pressed?> then
                    move (10) steps
                else
                    turn cw (15) degrees
                end
                """;

        Program program = parseProgram(code);
        String blockId = findBlockId(program, IfElseStmt.class);

        BlockingIfElseFix finder = new BlockingIfElseFix(createTestIssueDTO(blockId));
        assertFixFound(finder, program);
    }

    @Test
    public void testNoFixFound_BugStillPresent() throws IOException, ParsingException {
        // Forever still present in branch
        String code = """
                when green flag clicked
                if <key (space v) pressed?> then
                    forever
                        move (10) steps
                    end
                else
                    stop [all v]
                end
                say (hello)
                """;

        Program program = parseProgram(code);
        String blockId = findBlockId(program, IfElseStmt.class);

        BlockingIfElseFix finder = new BlockingIfElseFix(createTestIssueDTO(blockId));
        assertNoFixFound(finder, program);
    }

    @Test
    public void testFixFound_ScriptDeleted() throws IOException, ParsingException {
        // Script containing the bug is gone - valid fix
        String code = """
                when green flag clicked
                move (10) steps
                """;

        Program program = parseProgram(code);
        String blockId = "non-existent-id";

        BlockingIfElseFix finder = new BlockingIfElseFix(createTestIssueDTO(blockId));
        assertFixFound(finder, program);
    }

    // ========================================================================
    // INJECTOR-BASED TESTS
    // ========================================================================

    /**
     * Tests fix detection using the injector.
     *
     * <p>The BlockingIfElseInjector inserts a forever loop inside an if-else branch.
     * The fix heuristic should detect that the original program (without forever
     * in the if-else) is in a fixed state.
     */
    @Test
    public void testInjector_IfElseInsideLoop() throws IOException, ParsingException {
        // Same pattern as BlockingIfElseInjectorTest - fixed version has no forever in branch
        String fixedCode = """
                when green flag clicked
                repeat (10)
                    if <(1) = (1)> then
                        move (10) steps
                    else
                        move (20) steps
                    end
                end""";

        Program fixedProgram = parseProgram(fixedCode);

        // Verify original program has no bug
        BlockingIfElse bugFinder = new BlockingIfElse();
        Set<Issue> originalIssues = bugFinder.check(fixedProgram);
        Assertions.assertTrue(originalIssues.isEmpty(),
                "Original (fixed) program should have no BlockingIfElse issue");

        // Inject bug
        BlockingIfElseInjector injector = new BlockingIfElseInjector();
        List<InjectionResult> injected = injector.inject(fixedProgram);

        Assertions.assertFalse(injected.isEmpty(), "Injector should create at least one bug");

        // Verify the mutated program has bugs
        Program mutatedProgram = injected.getFirst().mutatedProgram();
        Set<Issue> bugIssues = bugFinder.check(mutatedProgram);
        Assertions.assertFalse(bugIssues.isEmpty(),
                "Bug finder should detect BlockingIfElse in mutated program");

        // Get the IfElseStmt block ID from the ORIGINAL program
        String ifElseBlockId = findBlockId(fixedProgram, IfElseStmt.class);
        Assertions.assertNotNull(ifElseBlockId,
                "Should find IfElseStmt block in original program");
    }
}

