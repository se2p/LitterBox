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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.MissingCloneInitialization;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.CreateCloneOf;
import de.uni_passau.fim.se2.litterbox.injection.InjectionResult;
import de.uni_passau.fim.se2.litterbox.injection.bugs.MissingCloneInitializationInjector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Tests for MissingCloneInitializationFix.
 * Bug: Create clone without "when I start as a clone" handler.
 * Fix: Handler now exists.
 */
public class MissingCloneInitializationFixTest extends AbstractFixHeuristicTest {

    @Test
    public void testFixFound_HandlerAdded() throws IOException, ParsingException {
        // Clone created and handler exists - fixed
        String code = """
                when green flag clicked
                create clone of (myself v)

                when I start as a clone
                move (10) steps
                """;

        Program program = parseProgram(code);
        String blockId = findBlockId(program, de.uni_passau.fim.se2.litterbox.ast.model.statement.common.CreateCloneOf.class);

        MissingCloneInitializationFix finder = new MissingCloneInitializationFix(createTestIssueDTO(blockId));
        assertFixFound(finder, program);
    }

    @Test
    public void testNoFixFound_HandlerStillMissing() throws IOException, ParsingException {
        // Clone created but handler still missing
        String code = """
                when green flag clicked
                create clone of (myself v)
                """;

        Program program = parseProgram(code);
        String blockId = findBlockId(program, de.uni_passau.fim.se2.litterbox.ast.model.statement.common.CreateCloneOf.class);

        MissingCloneInitializationFix finder = new MissingCloneInitializationFix(createTestIssueDTO(blockId));
        assertNoFixFound(finder, program);
    }

    @Test
    public void testFixFound_ScriptDeleted() throws IOException, ParsingException {
        // Fix: Script containing create clone is gone (deleted)
        String code = """
                when green flag clicked
                move (10) steps
                """;

        Program program = parseProgram(code);
        String blockId = "non-existent-id";

        MissingCloneInitializationFix finder = new MissingCloneInitializationFix(createTestIssueDTO(blockId));
        assertFixFound(finder, program);
    }

    // ========================================================================
    // INJECTOR-BASED TESTS
    // ========================================================================

    @Test
    public void testInjector_HandlersRemoved() throws IOException, ParsingException {
        // Fixed: Clone is created AND handled
        String fixedCode = """
                when green flag clicked
                create clone of (myself v)

                when I start as a clone
                move (10) steps
                """;

        Program fixedProgram = parseProgram(fixedCode);

        // Verify original program has no bug
        MissingCloneInitialization bugFinder = new MissingCloneInitialization();
        Set<Issue> originalIssues = bugFinder.check(fixedProgram);
        Assertions.assertTrue(originalIssues.isEmpty(),
                "Original (fixed) program should have no MissingCloneInitialization issue");

        // Inject bug: Removes 'when I start as a clone' handler
        MissingCloneInitializationInjector injector = new MissingCloneInitializationInjector();
        List<InjectionResult> injected = injector.inject(fixedProgram);

        Assertions.assertFalse(injected.isEmpty(), "Injector should create at least one bug");

        // Verify the mutated program has bugs
        Program mutatedProgram = injected.getFirst().mutatedProgram();
        Set<Issue> bugIssues = bugFinder.check(mutatedProgram);
        Assertions.assertFalse(bugIssues.isEmpty(),
                "Bug finder should detect MissingCloneInitialization in mutated program");

        // Get the CreateCloneOf block ID from the ORIGINAL program
        String blockId = findBlockId(fixedProgram, CreateCloneOf.class);
        Assertions.assertNotNull(blockId, "Should find CreateCloneOf block in original program");

        // The fix heuristic should detect this is fixed in the original program
        // (because the handler is present there)
        MissingCloneInitializationFix fixHeuristic = new MissingCloneInitializationFix(createTestIssueDTO(blockId));
        assertFixFound(fixHeuristic, fixedProgram);
    }
}
