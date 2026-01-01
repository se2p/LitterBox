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
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetVariableTo;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * Tests for RedundantAssignmentFix.
 *
 * <p>Bug: A variable is assigned a value that is immediately overwritten without being used.
 * <p>Fix: The assignment is no longer redundant (value is now used, or overwrite removed).
 */
public class RedundantAssignmentFixTest extends AbstractFixHeuristicTest {

    @Test
    public void testFixFound_AssignmentNowUsed() throws IOException, ParsingException {
        // Fixed: Variable is assigned and then used before being overwritten
        String code = """
                when green flag clicked
                set [myVar v] to (10)
                say (myVar)
                """;

        Program program = parseProgram(code);
        String blockId = findBlockId(program, SetVariableTo.class);

        RedundantAssignmentFix finder = new RedundantAssignmentFix(createTestIssueDTO(blockId));
        assertFixFound(finder, program);
    }

    @Test
    public void testFixFound_NoOverwrite() throws IOException, ParsingException {
        // Fixed: Variable is assigned but there's no immediate overwrite
        String code = """
                when green flag clicked
                set [myVar v] to (10)
                wait (1) seconds
                set [myVar v] to (20)
                """;

        Program program = parseProgram(code);
        String blockId = findBlockId(program, SetVariableTo.class);

        RedundantAssignmentFix finder = new RedundantAssignmentFix(createTestIssueDTO(blockId));
        assertFixFound(finder, program);
    }

    @Test
    public void testNoFixFound_RedundantAssignmentStillPresent() throws IOException, ParsingException {
        // Variable assigned and immediately overwritten
        String code = """
                when green flag clicked
                set [myVar v] to (10)
                set [myVar v] to (20)
                """;

        Program program = parseProgram(code);
        // Both assignments refer to myVar, pick the first one which is overwritten
        String blockId = findBlockId(program, SetVariableTo.class);

        RedundantAssignmentFix finder = new RedundantAssignmentFix(createTestIssueDTO(blockId));
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

        RedundantAssignmentFix finder = new RedundantAssignmentFix(createTestIssueDTO(blockId));
        assertFixFound(finder, program);
    }

    @Test
    public void testInjector_RedundantAssignment() throws IOException, ParsingException {
        // Start with a fixed program - no redundant assignment
        String fixedCode = """
                when green flag clicked
                set [myVar v] to (10)
                say (myVar)""";

        Program fixedProgram = parseProgram(fixedCode);

        // Inject bug
        de.uni_passau.fim.se2.litterbox.injection.bugs.RedundantAssignmentInjector injector = new de.uni_passau.fim.se2.litterbox.injection.bugs.RedundantAssignmentInjector();
        java.util.List<de.uni_passau.fim.se2.litterbox.injection.InjectionResult> injected = injector.inject(fixedProgram);

        org.junit.jupiter.api.Assertions.assertFalse(injected.isEmpty(), "Injector should create at least one bug");

        // Verify the mutated program has the bug
        Program mutatedProgram = injected.getFirst().mutatedProgram();
        de.uni_passau.fim.se2.litterbox.analytics.bugs.RedundantAssignment bugFinder = new de.uni_passau.fim.se2.litterbox.analytics.bugs.RedundantAssignment();
        java.util.Set<de.uni_passau.fim.se2.litterbox.analytics.Issue> bugIssues = bugFinder.check(mutatedProgram);
        org.junit.jupiter.api.Assertions.assertFalse(bugIssues.isEmpty(), "Bug finder should detect RedundantAssignment in mutated program");

        // Get the block ID
        de.uni_passau.fim.se2.litterbox.analytics.Issue bugIssue = bugIssues.iterator().next();
        String bugBlockId = de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil.getBlockId(bugIssue.getCodeLocation());

        // The fix heuristic should detect a fix in the original program
        RedundantAssignmentFix fixHeuristic = new RedundantAssignmentFix(createTestIssueDTO(bugBlockId));
        assertFixFound(fixHeuristic, fixedProgram);
    }
}
