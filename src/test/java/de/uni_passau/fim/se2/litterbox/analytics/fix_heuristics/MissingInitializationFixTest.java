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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.MissingInitialization;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.ChangeVariableBy;
import de.uni_passau.fim.se2.litterbox.injection.InjectionResult;
import de.uni_passau.fim.se2.litterbox.injection.bugs.MissingInitializationInjector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Tests for MissingInitializationFix heuristic.
 *
 * <p>The bug is reported when a variable is used before it is initialized.
 * The fix is detected when the variable use is now properly initialized before use.
 */
public class MissingInitializationFixTest extends AbstractFixHeuristicTest {

    @Test
    public void testFixFound_VariableInitialized() throws IOException, ParsingException {
        // Fix: Variable is initialized before it is used
        String source = """
                when green flag clicked
                set [myVar v] to (0)
                change [myVar v] by (1)
                """;

        Program program = parseProgram(source);
        String bugId = findBlockId(program, ChangeVariableBy.class);

        MissingInitializationFix heuristic = new MissingInitializationFix(createTestIssueDTO(bugId));
        assertFixFound(heuristic, program);
    }

    @Test
    public void testNoFix_VariableNotInitialized() throws IOException, ParsingException {
        // No Fix: Variable is used without being initialized first
        String source = """
                when green flag clicked
                change [myVar v] by (1)
                """;

        Program program = parseProgram(source);
        String bugId = findBlockId(program, ChangeVariableBy.class);

        MissingInitializationFix heuristic = new MissingInitializationFix(createTestIssueDTO(bugId));
        assertNoFixFound(heuristic, program);
    }

    @Test
    public void testFixFound_BlockDeleted() throws IOException, ParsingException {
        // Fix: The buggy block/script was deleted
        String source = """
                when green flag clicked
                move (10) steps
                """;

        Program program = parseProgram(source);

        MissingInitializationFix heuristic = new MissingInitializationFix(createTestIssueDTO("non_existent_id"));
        assertFixFound(heuristic, program);
    }

    // ========================================================================
    // INJECTOR-BASED TESTS
    // ========================================================================

    @Test
    public void testInjector_InitializationRemoved() throws IOException, ParsingException {
        // Fixed: Variable initialized before usage
        String fixedCode = """
                when green flag clicked
                set [myVar v] to (0)
                change [myVar v] by (1)
                """;

        Program fixedProgram = parseProgram(fixedCode);

        // Verify original program has no bug
        MissingInitialization bugFinder = new MissingInitialization();
        Set<Issue> originalIssues = bugFinder.check(fixedProgram);
        Assertions.assertTrue(originalIssues.isEmpty(),
                "Original (fixed) program should have no MissingInitialization issue");

        // Inject bug: Removes 'set variable'
        MissingInitializationInjector injector = new MissingInitializationInjector();
        List<InjectionResult> injected = injector.inject(fixedProgram);

        Assertions.assertFalse(injected.isEmpty(), "Injector should create at least one bug");

        // Verify the mutated program has bugs
        Program mutatedProgram = injected.getFirst().mutatedProgram();
        Set<Issue> bugIssues = bugFinder.check(mutatedProgram);
        Assertions.assertFalse(bugIssues.isEmpty(),
                "Bug finder should detect MissingInitialization in mutated program");

        // Get the ChangeVariableBy block ID from the ORIGINAL program
        String blockId = findBlockId(fixedProgram, ChangeVariableBy.class);
        Assertions.assertNotNull(blockId, "Should find ChangeVariableBy block in original program");

        // The fix heuristic should detect this is fixed in the original program
        MissingInitializationFix fixHeuristic = new MissingInitializationFix(createTestIssueDTO(blockId));
        assertFixFound(fixHeuristic, fixedProgram);
    }
}
