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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.MissingPenUp;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.pen.PenDownStmt;
import de.uni_passau.fim.se2.litterbox.injection.InjectionResult;
import de.uni_passau.fim.se2.litterbox.injection.bugs.MissingPenUpInjector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class MissingPenUpFixTest extends AbstractFixHeuristicTest {

    @Test
    public void testFixFound() throws IOException, ParsingException {
        // Fix: 'pen up' is present in the actor
        String source = """
                when green flag clicked
                pen down
                pen up
                """;

        Program program = parseProgram(source);
        String bugLocationBlockId = findBlockId(program, PenDownStmt.class);

        MissingPenUpFix heuristic = new MissingPenUpFix(createTestIssueDTO(bugLocationBlockId));
        assertFixFound(heuristic, program);
    }

    @Test
    public void testFixFoundAlternativeScript() throws IOException, ParsingException {
        // Fix: 'pen up' is present in a different script of the same actor
        String source = """
                when green flag clicked
                pen down

                when I receive [start v]
                pen up
                """;

        Program program = parseProgram(source);
        String bugLocationBlockId = findBlockId(program, PenDownStmt.class);

        MissingPenUpFix heuristic = new MissingPenUpFix(createTestIssueDTO(bugLocationBlockId));
        assertFixFound(heuristic, program);
    }

    @Test
    public void testNoFix() throws IOException, ParsingException {
        // No Fix: No 'pen up'
        String source = """
                when green flag clicked
                pen down
                move (10) steps
                """;

        Program program = parseProgram(source);
        String bugLocationBlockId = findBlockId(program, PenDownStmt.class);

        MissingPenUpFix heuristic = new MissingPenUpFix(createTestIssueDTO(bugLocationBlockId));
        assertNoFixFound(heuristic, program);
    }

    @Test
    public void testFixFound_BlockDeleted() throws IOException, ParsingException {
        // Fix: The buggy pen down is removed
        String source = """
                when green flag clicked
                pen up
                """;

        Program program = parseProgram(source);
        MissingPenUpFix heuristic = new MissingPenUpFix(createTestIssueDTO("non-existent-id"));
        assertFixFound(heuristic, program);
    }

    // ========================================================================
    // INJECTOR-BASED TESTS
    // ========================================================================

    @Test
    public void testInjector_PenUpRemoved() throws IOException, ParsingException {
        // Fixed: Pen Up used after Pen Down
        String fixedCode = """
                when green flag clicked
                pen down
                pen up
                """;

        Program fixedProgram = parseProgram(fixedCode);

        // Verify original program has no bug
        MissingPenUp bugFinder = new MissingPenUp();
        Set<Issue> originalIssues = bugFinder.check(fixedProgram);
        Assertions.assertTrue(originalIssues.isEmpty(),
                "Original (fixed) program should have no MissingPenUp issue");

        // Inject bug: Removes 'pen up'
        MissingPenUpInjector injector = new MissingPenUpInjector();
        List<InjectionResult> injected = injector.inject(fixedProgram);

        Assertions.assertFalse(injected.isEmpty(), "Injector should create at least one bug");

        // Verify the mutated program has bugs
        Program mutatedProgram = injected.getFirst().mutatedProgram();
        Set<Issue> bugIssues = bugFinder.check(mutatedProgram);
        Assertions.assertFalse(bugIssues.isEmpty(),
                "Bug finder should detect MissingPenUp in mutated program");

        // Get the PenDown block ID from the ORIGINAL program
        String blockId = findBlockId(fixedProgram, PenDownStmt.class);
        Assertions.assertNotNull(blockId, "Should find PenDownStmt block in original program");

        // The fix heuristic should detect this is fixed in the original program
        MissingPenUpFix fixHeuristic = new MissingPenUpFix(createTestIssueDTO(blockId));
        assertFixFound(fixHeuristic, fixedProgram);
    }
}
