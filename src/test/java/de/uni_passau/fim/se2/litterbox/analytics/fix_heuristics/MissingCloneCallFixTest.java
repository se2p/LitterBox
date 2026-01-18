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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.MissingCloneCall;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.event.StartedAsClone;
import de.uni_passau.fim.se2.litterbox.injection.InjectionResult;
import de.uni_passau.fim.se2.litterbox.injection.bugs.MissingCloneCallInjector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class MissingCloneCallFixTest extends AbstractFixHeuristicTest {

    @Test
    public void testFixFoundSpecificSprite() throws IOException, ParsingException {
        // Fix: 'create clone of (Sprite1 v)' is called
        String source = """
                when I start as a clone
                move (10) steps

                when green flag clicked
                create clone of (Sprite1 v)
                """;

        Program program = parseProgram(source);
        String bugLocationBlockId = findBlockId(program, StartedAsClone.class);

        MissingCloneCallFix heuristic = new MissingCloneCallFix(createTestIssueDTO(bugLocationBlockId));
        assertFixFound(heuristic, program);
    }

    @Test
    public void testFixFoundMyselfDropdown() throws IOException, ParsingException {
        // Fix: 'create clone of (myself v)' is called
        String source = """
                when I start as a clone
                move (10) steps

                when green flag clicked
                create clone of (myself v)
                """;

        Program program = parseProgram(source);
        String bugLocationBlockId = findBlockId(program, StartedAsClone.class);

        MissingCloneCallFix heuristic = new MissingCloneCallFix(createTestIssueDTO(bugLocationBlockId));
        assertFixFound(heuristic, program);
    }

    @Test
    public void testNoFixNoClone() throws IOException, ParsingException {
        // No Fix: No cloning is happening
        String source = """
                when I start as a clone
                move (10) steps

                when green flag clicked
                move (10) steps
                """;

        Program program = parseProgram(source);
        String bugLocationBlockId = findBlockId(program, StartedAsClone.class);

        MissingCloneCallFix heuristic = new MissingCloneCallFix(createTestIssueDTO(bugLocationBlockId));
        assertNoFixFound(heuristic, program);
    }

    @Test
    public void testFixFound_BlockDeleted() throws IOException, ParsingException {
        // Fix: The buggy block (event) was deleted
        String source = """
                when green flag clicked
                create clone of (myself v)
                """;

        Program program = parseProgram(source);
        // The block is gone, so heuristic should report fix (deletion)
        MissingCloneCallFix heuristic = new MissingCloneCallFix(createTestIssueDTO("non-existent-id"));
        assertFixFound(heuristic, program);
    }

    // ========================================================================
    // MULTI-SPRITE TESTS
    // ========================================================================

    @Test
    public void testFixFound_ClonedFromOtherSprite() throws IOException, ParsingException {
        // Multi-sprite scenario: Sprite1 creates a clone of Sprite2,
        // and Sprite2 has the clone handler.
        String sprite1Code = """
                when green flag clicked
                create clone of (Sprite2 v)
                """;

        String sprite2Code = """
                when I start as a clone
                move (10) steps
                """;

        Program program = parseMultiSpriteProgram(sprite1Code, sprite2Code);

        // Find the StartedAsClone block in Sprite2
        String bugId = findBlockId(program, StartedAsClone.class);
        Assertions.assertNotNull(bugId, "Should find StartedAsClone block in Sprite2");

        // The bug is fixed because Sprite1 does create a clone of Sprite2
        MissingCloneCallFix heuristic = new MissingCloneCallFix(createTestIssueDTO(bugId));
        assertFixFound(heuristic, program);
    }

    @Test
    public void testNoFix_ClonedFromWrongSprite() throws IOException, ParsingException {
        // Multi-sprite scenario: Sprite2 has a clone handler but Sprite1
        // creates a clone of itself, not of Sprite2.
        String sprite1Code = """
                when green flag clicked
                create clone of (myself v)
                """;

        String sprite2Code = """
                when I start as a clone
                move (10) steps
                """;

        Program program = parseMultiSpriteProgram(sprite1Code, sprite2Code);

        // Find the StartedAsClone block in Sprite2
        String bugId = findBlockId(program, StartedAsClone.class);
        Assertions.assertNotNull(bugId, "Should find StartedAsClone block in Sprite2");

        // The bug is NOT fixed because Sprite1 clones itself, not Sprite2
        MissingCloneCallFix heuristic = new MissingCloneCallFix(createTestIssueDTO(bugId));
        assertNoFixFound(heuristic, program);
    }

    // ========================================================================
    // INJECTOR-BASED TESTS
    // ========================================================================

    @Test
    public void testInjector_CloneCallRemoved() throws IOException, ParsingException {
        // Fixed: Create clone IS called
        String fixedCode = """
                when I start as a clone
                move (10) steps

                when green flag clicked
                create clone of (myself v)
                """;

        Program fixedProgram = parseProgram(fixedCode);

        // Verify original program has no bug
        MissingCloneCall bugFinder = new MissingCloneCall();
        Set<Issue> originalIssues = bugFinder.check(fixedProgram);
        Assertions.assertTrue(originalIssues.isEmpty(),
                "Original (fixed) program should have no MissingCloneCall issue");

        // Inject bug: Removes 'create clone of'
        MissingCloneCallInjector injector = new MissingCloneCallInjector();
        List<InjectionResult> injected = injector.inject(fixedProgram);

        Assertions.assertFalse(injected.isEmpty(), "Injector should create at least one bug");

        // Verify the mutated program has bugs
        Program mutatedProgram = injected.getFirst().mutatedProgram();
        Set<Issue> bugIssues = bugFinder.check(mutatedProgram);
        Assertions.assertFalse(bugIssues.isEmpty(),
                "Bug finder should detect MissingCloneCall in mutated program");

        // Get the StartedAsClone block ID from the ORIGINAL program
        String bugId = findBlockId(fixedProgram, StartedAsClone.class);
        Assertions.assertNotNull(bugId, "Should find StartedAsClone block in original program");

        // The fix heuristic should detect this is fixed in the original program
        MissingCloneCallFix fixHeuristic = new MissingCloneCallFix(createTestIssueDTO(bugId));
        assertFixFound(fixHeuristic, fixedProgram);
    }
}
