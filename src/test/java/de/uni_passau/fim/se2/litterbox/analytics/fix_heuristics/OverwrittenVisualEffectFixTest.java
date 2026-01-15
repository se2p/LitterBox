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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.OverwrittenVisualEffect;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.SetGraphicEffectTo;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.injection.InjectionResult;
import de.uni_passau.fim.se2.litterbox.injection.bugs.OverwrittenVisualEffectInjector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Tests for OverwrittenVisualEffectFix.
 * Bug: Visual effects overwritten without delay.
 * Fix: Delay added between effect changes or redundant effect removed.
 */
public class OverwrittenVisualEffectFixTest extends AbstractFixHeuristicTest {

    @Test
    public void testFixFound_DelayAdded() throws IOException, ParsingException {
        // Effect with wait before - fix reported
        String code = """
                when green flag clicked
                set [ghost v] effect to (50)
                wait (1) seconds
                set [ghost v] effect to (100)
                """;

        Program program = parseProgram(code);
        String blockId = findBlockId(program, SetGraphicEffectTo.class);

        OverwrittenVisualEffectFix finder = new OverwrittenVisualEffectFix(createTestIssueDTO(blockId));
        assertFixFound(finder, program);
    }

    @Test
    public void testNoFixFound_EffectOverwritten() throws IOException, ParsingException {
        // Effect overwritten immediately
        String code = """
                when green flag clicked
                set [ghost v] effect to (50)
                set [ghost v] effect to (100)
                """;

        Program program = parseProgram(code);
        String blockId = findBlockId(program, SetGraphicEffectTo.class);

        OverwrittenVisualEffectFix finder = new OverwrittenVisualEffectFix(createTestIssueDTO(blockId));
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

        OverwrittenVisualEffectFix finder = new OverwrittenVisualEffectFix(createTestIssueDTO(blockId));
        assertFixFound(finder, program);
    }

    // ========================================================================
    // INJECTOR-BASED TESTS
    // ========================================================================

    @Test
    public void testInjector_VisualEffectOverwritten() throws IOException, ParsingException {
        // Start with a valid program (effect set once)
        String fixedCode = """
                when green flag clicked
                set [ghost v] effect to (50)
                """;
        Program fixedProgram = parseProgram(fixedCode);

        // Verify clean
        OverwrittenVisualEffect bugFinder = new OverwrittenVisualEffect();
        Set<Issue> originalIssues = bugFinder.check(fixedProgram);
        org.junit.jupiter.api.Assertions.assertTrue(originalIssues.isEmpty(), "Original program should be clean");

        // Inject bug (likely adds another set effect immediately after)
        OverwrittenVisualEffectInjector injector = new OverwrittenVisualEffectInjector();
        List<InjectionResult> results = injector.inject(fixedProgram);
        Assertions.assertFalse(results.isEmpty(), "Injector should produce results");

        // Verify bug in mutated
        Program mutatedProgram = results.getFirst().mutatedProgram();
        Set<Issue> bugIssues = bugFinder.check(mutatedProgram);
        Assertions.assertFalse(bugIssues.isEmpty(), "Mutated program should have bugs");

        // Fix check: The bug is the overwritten effect. In the original program, that block doesn't exist (if added)
        // or is not overwritten.
        String bugBlockId = AstNodeUtil.getBlockId(bugIssues.iterator().next().getCodeLocation());

        OverwrittenVisualEffectFix fix = new OverwrittenVisualEffectFix(createTestIssueDTO(bugBlockId));
        assertFixFound(fix, fixedProgram);
    }
}
