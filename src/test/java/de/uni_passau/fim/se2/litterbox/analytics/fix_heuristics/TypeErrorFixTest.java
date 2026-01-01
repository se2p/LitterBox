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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.TypeError;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.Add;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.injection.InjectionResult;
import de.uni_passau.fim.se2.litterbox.injection.bugs.TypeErrorInjector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Tests for TypeErrorFix.
 * Bug: Type mismatch in expression.
 * Fix: Types now match correctly, or buggy block was deleted.
 */
public class TypeErrorFixTest extends AbstractFixHeuristicTest {

    @Test
    public void testFixFound_TypesMatch() throws IOException, ParsingException {
        // Numeric addition with both numbers - fix reported
        String code = """
                when green flag clicked
                set [result v] to ((10) + (5))
                """;

        Program program = parseProgram(code);
        String blockId = findBlockId(program, Add.class);

        TypeErrorFix finder = new TypeErrorFix(createTestIssueDTO(blockId));
        assertFixFound(finder, program);
    }

    @Test
    public void testNoFixFound_TypesMismatch() throws IOException, ParsingException {
        // Types still mismatch (SetXTo expects number, got string)
        String code = """
                when green flag clicked
                set x to [hello]
                """;

        Program program = parseProgram(code);
        String blockId = findBlockId(program, de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.SetXTo.class);

        TypeErrorFix finder = new TypeErrorFix(createTestIssueDTO(blockId));
        assertNoFixFound(finder, program);
    }

    @Test
    public void testFixFound_BlockDeleted() throws IOException, ParsingException {
        // Bug was fixed by deleting the buggy block
        String code = """
                when green flag clicked
                move (10) steps
                """;

        Program program = parseProgram(code);
        String blockId = "non-existent-id";

        TypeErrorFix finder = new TypeErrorFix(createTestIssueDTO(blockId));
        // Deleting the buggy block is a valid fix
        assertFixFound(finder, program);
    }

    // ========================================================================
    // INJECTOR-BASED TESTS
    // ========================================================================

    @Test
    public void testInjector_NumericExpression() throws IOException, ParsingException {
        // Start with a fixed program - move with numeric expression
        String fixedCode = """
                when green flag clicked
                move (10) steps""";

        Program fixedProgram = parseProgram(fixedCode);

        // Verify original program has no TypeError
        TypeError bugFinder = new TypeError();
        Set<Issue> originalIssues = bugFinder.check(fixedProgram);
        Assertions.assertTrue(originalIssues.isEmpty(),
                "Original (fixed) program should have no TypeError issue");

        // Inject bug
        TypeErrorInjector injector = new TypeErrorInjector();
        List<InjectionResult> injected = injector.inject(fixedProgram);

        Assertions.assertFalse(injected.isEmpty(), "Injector should create at least one bug");

        // Verify the mutated program has TypeError
        Program mutatedProgram = injected.getFirst().mutatedProgram();
        Set<Issue> bugIssues = bugFinder.check(mutatedProgram);
        Assertions.assertFalse(bugIssues.isEmpty(),
                "Bug finder should detect TypeError in mutated program");

        // Get the block ID of the buggy expression
        Issue bugIssue = bugIssues.iterator().next();
        String bugBlockId = AstNodeUtil.getBlockId(bugIssue.getCodeLocation());
        Assertions.assertNotNull(bugBlockId, "Bug issue should have a block ID");

        // The fix heuristic should detect a fix in the original program
        TypeErrorFix fixHeuristic = new TypeErrorFix(createTestIssueDTO(bugBlockId));
        assertFixFound(fixHeuristic, fixedProgram);
    }
}

