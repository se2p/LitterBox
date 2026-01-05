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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.MissingEraseAll;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.pen.PenDownStmt;
import de.uni_passau.fim.se2.litterbox.injection.InjectionResult;
import de.uni_passau.fim.se2.litterbox.injection.bugs.MissingEraseAllInjector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class MissingEraseAllFixTest extends AbstractFixHeuristicTest {

    @Test
    public void testFixFound() throws IOException, ParsingException {
        // Fix: 'erase all' is present in the actor
        String source = """
                when green flag clicked
                pen down
                erase all
                """;

        Program program = parseProgram(source);
        String bugLocationBlockId = findBlockId(program, PenDownStmt.class);

        MissingEraseAllFix heuristic = new MissingEraseAllFix(createTestIssueDTO(bugLocationBlockId));
        assertFixFound(heuristic, program);
    }

    @Test
    public void testFixFoundClear() throws IOException, ParsingException {
        // Fix: 'clear' (alias for erase all) is present
        String source = """
                when green flag clicked
                pen down
                clear
                """;

        Program program = parseProgram(source);
        String bugLocationBlockId = findBlockId(program, PenDownStmt.class);

        MissingEraseAllFix heuristic = new MissingEraseAllFix(createTestIssueDTO(bugLocationBlockId));
        assertFixFound(heuristic, program);
    }

    @Test
    public void testFixFoundAlternativeScript() throws IOException, ParsingException {
        // Fix: 'erase all' is present in a different script of the same actor
        String source = """
                when green flag clicked
                pen down

                when I receive [start v]
                erase all
                """;

        Program program = parseProgram(source);
        String bugLocationBlockId = findBlockId(program, PenDownStmt.class);

        MissingEraseAllFix heuristic = new MissingEraseAllFix(createTestIssueDTO(bugLocationBlockId));
        assertFixFound(heuristic, program);
    }

    @Test
    public void testNoFix() throws IOException, ParsingException {
        // No Fix: No 'erase all' or 'clear'
        String source = """
                when green flag clicked
                pen down
                move (10) steps
                """;

        Program program = parseProgram(source);
        String bugLocationBlockId = findBlockId(program, PenDownStmt.class);

        MissingEraseAllFix heuristic = new MissingEraseAllFix(createTestIssueDTO(bugLocationBlockId));
        assertNoFixFound(heuristic, program);
    }

    @Test
    public void testFixFound_BlockDeleted() throws IOException, ParsingException {
        // Fix: The buggy pen down is removed
        String source = """
                when green flag clicked
                erase all
                """;

        Program program = parseProgram(source);
        MissingEraseAllFix heuristic = new MissingEraseAllFix(createTestIssueDTO("non-existent-id"));
        assertFixFound(heuristic, program);
    }

    // ========================================================================
    // INJECTOR-BASED TESTS
    // ========================================================================

    @Test
    public void testInjector_EraseAllRemoved() throws IOException, ParsingException {
        // Fixed: Pen Down used WITH erase all
        String fixedCode = """
                when green flag clicked
                erase all
                pen down
                """;

        Program fixedProgram = parseProgram(fixedCode);

        // Verify original program has no bug
        MissingEraseAll bugFinder = new MissingEraseAll();
        Set<Issue> originalIssues = bugFinder.check(fixedProgram);
        Assertions.assertTrue(originalIssues.isEmpty(),
                "Original (fixed) program should have no MissingEraseAll issue");

        // Inject bug: Removes 'erase all'
        MissingEraseAllInjector injector = new MissingEraseAllInjector();
        List<InjectionResult> injected = injector.inject(fixedProgram);

        Assertions.assertFalse(injected.isEmpty(), "Injector should create at least one bug");

        // Verify the mutated program has bugs
        Program mutatedProgram = injected.getFirst().mutatedProgram();
        Set<Issue> bugIssues = bugFinder.check(mutatedProgram);
        Assertions.assertFalse(bugIssues.isEmpty(),
                "Bug finder should detect MissingEraseAll in mutated program");

        // Get the PenDown block ID from the ORIGINAL program
        String blockId = findBlockId(fixedProgram, PenDownStmt.class);
        Assertions.assertNotNull(blockId, "Should find PenDownStmt block in original program");

        // The fix heuristic should detect this is fixed in the original program
        MissingEraseAllFix fixHeuristic = new MissingEraseAllFix(createTestIssueDTO(blockId));
        assertFixFound(fixHeuristic, fixedProgram);
    }
}
