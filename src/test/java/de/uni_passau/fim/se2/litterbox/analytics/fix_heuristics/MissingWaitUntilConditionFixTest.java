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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.MissingWaitUntilCondition;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitUntil;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.injection.InjectionResult;
import de.uni_passau.fim.se2.litterbox.injection.bugs.MissingWaitUntilConditionInjector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Tests for MissingWaitUntilConditionFix.
 * Bug: WaitUntil has unspecified boolean expression.
 * Fix: Condition is now specified.
 */
public class MissingWaitUntilConditionFixTest extends AbstractFixHeuristicTest {

    @Test
    public void testFixFound_ConditionSpecified() throws IOException, ParsingException {
        // WaitUntil with a real condition - should report fix
        String code = """
                when green flag clicked
                wait until <key (space v) pressed?>
                move (10) steps
                """;

        Program program = parseProgram(code);
        String blockId = findBlockId(program, WaitUntil.class);

        MissingWaitUntilConditionFix finder = new MissingWaitUntilConditionFix(createTestIssueDTO(blockId));
        assertFixFound(finder, program);
    }

    @Test
    public void testNoFixFound_ConditionStillMissing() throws IOException, ParsingException {
        // WaitUntil still missing condition
        String code = """
                when green flag clicked
                wait until <>
                move (10) steps
                """;

        Program program = parseProgram(code);
        String blockId = findBlockId(program, WaitUntil.class);

        MissingWaitUntilConditionFix finder = new MissingWaitUntilConditionFix(createTestIssueDTO(blockId));
        assertNoFixFound(finder, program);
    }

    @Test
    public void testFixFound_ScriptDeleted() throws IOException, ParsingException {
        // Fix: Script containing the bug is gone
        String code = """
                when green flag clicked
                move (10) steps
                """;

        Program program = parseProgram(code);
        String blockId = "non-existent-id";

        MissingWaitUntilConditionFix finder = new MissingWaitUntilConditionFix(createTestIssueDTO(blockId));
        assertFixFound(finder, program);
    }

    // ========================================================================
    // INJECTOR-BASED TESTS
    // ========================================================================

    @Test
    public void testInjector_WaitUntil() throws IOException, ParsingException {
        // Start with a fixed program - wait until with a condition
        String fixedCode = """
                when green flag clicked
                wait until <key (space v) pressed?>
                move (10) steps""";

        Program fixedProgram = parseProgram(fixedCode);

        // Verify original program has no MissingWaitUntilCondition
        MissingWaitUntilCondition bugFinder = new MissingWaitUntilCondition();
        Set<Issue> originalIssues = bugFinder.check(fixedProgram);
        Assertions.assertTrue(originalIssues.isEmpty(),
                "Original (fixed) program should have no MissingWaitUntilCondition issue");

        // Inject bug
        MissingWaitUntilConditionInjector injector = new MissingWaitUntilConditionInjector();
        List<InjectionResult> injected = injector.inject(fixedProgram);

        Assertions.assertFalse(injected.isEmpty(), "Injector should create at least one bug");

        // Verify the mutated program has the bug
        Program mutatedProgram = injected.getFirst().mutatedProgram();
        Set<Issue> bugIssues = bugFinder.check(mutatedProgram);
        Assertions.assertFalse(bugIssues.isEmpty(),
                "Bug finder should detect MissingWaitUntilCondition in mutated program");

        // Get the block ID
        Issue bugIssue = bugIssues.iterator().next();
        String bugBlockId = AstNodeUtil.getBlockId(bugIssue.getCodeLocation());
        Assertions.assertNotNull(bugBlockId, "Bug issue should have a block ID");

        // The fix heuristic should detect a fix in the original program
        MissingWaitUntilConditionFix fixHeuristic = new MissingWaitUntilConditionFix(createTestIssueDTO(bugBlockId));
        assertFixFound(fixHeuristic, fixedProgram);
    }
}

