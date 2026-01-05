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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.RecursiveCloning;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.CreateCloneOf;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.injection.InjectionResult;
import de.uni_passau.fim.se2.litterbox.injection.bugs.RecursiveCloningInjector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class RecursiveCloningFixTest extends AbstractFixHeuristicTest {

    @Test
    public void testFixFound() throws IOException, ParsingException {
        // Fix: Script has "create clone of myself" AND "delete this clone"
        String source = """
                when I start as a clone
                create clone of (myself v)
                delete this clone
                """;

        Program program = parseProgram(source);
        String bugLocationBlockId = findBlockId(program, CreateCloneOf.class);

        RecursiveCloningFix heuristic = new RecursiveCloningFix(createTestIssueDTO(bugLocationBlockId));
        assertFixFound(heuristic, program);
    }

    @Test
    public void testNoFix() throws IOException, ParsingException {
        // No Fix: Missing "delete this clone"
        String source = """
                when I start as a clone
                create clone of (myself v)
                """;

        Program program = parseProgram(source);
        String bugLocationBlockId = findBlockId(program, CreateCloneOf.class);

        RecursiveCloningFix heuristic = new RecursiveCloningFix(createTestIssueDTO(bugLocationBlockId));
        assertNoFixFound(heuristic, program);
    }

    @Test
    public void testFixFound_BlockDeleted() throws IOException, ParsingException {
        // Silent: Buggy block deleted
        String source = """
                when I start as a clone
                delete this clone
                """;

        Program program = parseProgram(source);
        // bugLocationBlockId is gone

        RecursiveCloningFix heuristic = new RecursiveCloningFix(createTestIssueDTO("non_existent_id"));
        assertFixFound(heuristic, program);
    }

    // ========================================================================
    // INJECTOR-BASED TESTS
    // ========================================================================

    @Test
    public void testInjector_RecursiveClone() throws IOException, ParsingException {
        // Start with a valid program (no recursive clone loops)
        // Use a script that does NOT delete the clone, but also doesn't create one recursively.
        // The injector looks for scripts starting with "when I start as a clone" that do NOT have "delete this clone".
        String fixedCode = """
                when I start as a clone
                move (10) steps
                """;
        Program fixedProgram = parseProgram(fixedCode);

        // Verify clean
        RecursiveCloning bugFinder = new RecursiveCloning();
        Set<Issue> originalIssues = bugFinder.check(fixedProgram);
        Assertions.assertTrue(originalIssues.isEmpty(), "Original program should be clean");

        // Inject bug (adds 'create clone of myself' without delete, or just adds the loop)
        RecursiveCloningInjector injector = new RecursiveCloningInjector();
        List<InjectionResult> results = injector.inject(fixedProgram);
        Assertions.assertFalse(results.isEmpty(), "Injector should produce results");

        // Verify bug in mutated
        Program mutatedProgram = results.getFirst().mutatedProgram();
        Set<Issue> bugIssues = bugFinder.check(mutatedProgram);
        Assertions.assertFalse(bugIssues.isEmpty(), "Mutated program should have bugs");

        // Fix check: The bug is the 'create clone' call. In the original, it doesn't exist.
        String bugBlockId = AstNodeUtil.getBlockId(bugIssues.iterator().next().getCodeLocation());

        RecursiveCloningFix fix = new RecursiveCloningFix(createTestIssueDTO(bugBlockId));
        assertFixFound(fix, fixedProgram);
    }
}
