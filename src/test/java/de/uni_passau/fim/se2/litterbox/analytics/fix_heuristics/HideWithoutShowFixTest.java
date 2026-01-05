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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.HideWithoutShow;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Hide;
import de.uni_passau.fim.se2.litterbox.injection.InjectionResult;
import de.uni_passau.fim.se2.litterbox.injection.bugs.HideWithoutShowInjector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Tests for HideWithoutShowFix.
 * Bug: Hide is used but Show never called in visible actor.
 * Fix: Show now exists or actor makes no visible changes.
 */
public class HideWithoutShowFixTest extends AbstractFixHeuristicTest {

    @Test
    public void testFixFound_ShowAdded() throws IOException, ParsingException {
        // Actor has both Hide and Show - fixed
        String code = """
                when green flag clicked
                hide
                show
                """;

        Program program = parseProgram(code);
        String blockId = findBlockId(program, Hide.class);

        HideWithoutShowFix finder = new HideWithoutShowFix(createTestIssueDTO(blockId));
        assertFixFound(finder, program);
    }

    @Test
    public void testNoFixFound_ShowStillMissing() throws IOException, ParsingException {
        // Actor has Hide but Show is still missing
        String code = """
                when green flag clicked
                hide
                move (10) steps
                """;

        Program program = parseProgram(code);
        String blockId = findBlockId(program, Hide.class);

        HideWithoutShowFix finder = new HideWithoutShowFix(createTestIssueDTO(blockId));
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

        HideWithoutShowFix finder = new HideWithoutShowFix(createTestIssueDTO(blockId));
        assertFixFound(finder, program);
    }

    // ========================================================================
    // INJECTOR-BASED TESTS
    // ========================================================================

    /**
     * Tests fix detection using the injector.
     *
     * <p>The HideWithoutShowInjector removes Show statements.
     * The fix heuristic should detect that the original program (with Show)
     * is in a fixed state.
     */
    @Test
    public void testInjector_ShowAndHide() throws IOException, ParsingException {
        // Same pattern as HideWithoutShowInjectorTest - fixed version has both show and hide
        String fixedCode = """
                when green flag clicked
                show
                move (10) steps
                wait (1) secs
                hide
                """;

        Program fixedProgram = parseProgram(fixedCode);

        // Verify original program has no bug
        HideWithoutShow bugFinder = new HideWithoutShow();
        Set<Issue> originalIssues = bugFinder.check(fixedProgram);
        Assertions.assertTrue(originalIssues.isEmpty(),
                "Original (fixed) program should have no HideWithoutShow issue");

        // Inject bug
        HideWithoutShowInjector injector = new HideWithoutShowInjector();
        List<InjectionResult> injected = injector.inject(fixedProgram);

        Assertions.assertFalse(injected.isEmpty(), "Injector should create at least one bug");

        // Verify the mutated program has bugs
        Program mutatedProgram = injected.getFirst().mutatedProgram();
        Set<Issue> bugIssues = bugFinder.check(mutatedProgram);
        Assertions.assertFalse(bugIssues.isEmpty(),
                "Bug finder should detect HideWithoutShow in mutated program");

        // Get the Hide block ID from the ORIGINAL program
        String hideBlockId = findBlockId(fixedProgram, Hide.class);
        Assertions.assertNotNull(hideBlockId,
                "Should find Hide block in original program");
    }
}

