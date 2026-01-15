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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.KeySetPosition;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.ChangeXBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.SetXTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.SetYTo;
import de.uni_passau.fim.se2.litterbox.injection.InjectionResult;
import de.uni_passau.fim.se2.litterbox.injection.bugs.KeySetPositionInjector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Tests for KeySetPositionFix heuristic.
 *
 *
 * <p>The bug is reported when an arrow key event uses SetX/SetY (absolute positioning)
 * instead of ChangeX/ChangeY (relative movement). Specifically:
 * <ul>
 *   <li>SetX with LEFT/RIGHT arrows is buggy</li>
 *   <li>SetY with UP/DOWN arrows is buggy</li>
 * </ul>
 *
 * <p>The fix is detected when:
 * <ul>
 *   <li>SetX/SetY is outside an arrow key context</li>
 *   <li>SetX is used with UP/DOWN arrows (mismatched pattern)</li>
 *   <li>SetY is used with LEFT/RIGHT arrows (mismatched pattern)</li>
 *   <li>Any non-arrow key is used</li>
 * </ul>
 */
public class KeySetPositionFixTest extends AbstractFixHeuristicTest {

    @Test
    public void testFixFound_NoKeyContext() throws IOException, ParsingException {
        // Fix: SetX used outside of any key context
        String source = """
                when green flag clicked
                set x to (0)
                """;

        Program program = parseProgram(source);
        String bugId = findBlockId(program, SetXTo.class);

        KeySetPositionFix heuristic = new KeySetPositionFix(createTestIssueDTO(bugId));
        assertFixFound(heuristic, program);
    }

    @Test
    public void testFixFound_NonArrowKey() throws IOException, ParsingException {
        // Fix: SetX used with space key (not an arrow key)
        String source = """
                when [space v] key pressed
                set x to (0)
                """;

        Program program = parseProgram(source);
        String bugId = findBlockId(program, SetXTo.class);

        KeySetPositionFix heuristic = new KeySetPositionFix(createTestIssueDTO(bugId));
        assertFixFound(heuristic, program);
    }

    @Test
    public void testFixFound_SetXWithUpArrow() throws IOException, ParsingException {
        // Fix: SetX used with UP arrow (bug is SetX with LEFT/RIGHT)
        String source = """
                when [up arrow v] key pressed
                set x to (0)
                """;

        Program program = parseProgram(source);
        String bugId = findBlockId(program, SetXTo.class);

        KeySetPositionFix heuristic = new KeySetPositionFix(createTestIssueDTO(bugId));
        assertFixFound(heuristic, program);
    }

    @Test
    public void testFixFound_SetYWithLeftArrow() throws IOException, ParsingException {
        // Fix: SetY used with LEFT arrow (bug is SetY with UP/DOWN)
        String source = """
                when [left arrow v] key pressed
                set y to (0)
                """;

        Program program = parseProgram(source);
        String bugId = findBlockId(program, SetYTo.class);

        KeySetPositionFix heuristic = new KeySetPositionFix(createTestIssueDTO(bugId));
        assertFixFound(heuristic, program);
    }

    @Test
    public void testNoFix_SetXWithLeftArrow() throws IOException, ParsingException {
        // No Fix: SetX used with LEFT arrow (this IS the bug pattern)
        String source = """
                when [left arrow v] key pressed
                set x to (0)
                """;

        Program program = parseProgram(source);
        String bugId = findBlockId(program, SetXTo.class);

        KeySetPositionFix heuristic = new KeySetPositionFix(createTestIssueDTO(bugId));
        assertNoFixFound(heuristic, program);
    }

    @Test
    public void testNoFix_SetYWithUpArrow() throws IOException, ParsingException {
        // No Fix: SetY used with UP arrow (this IS the bug pattern)
        String source = """
                when [up arrow v] key pressed
                set y to (0)
                """;

        Program program = parseProgram(source);
        String bugId = findBlockId(program, SetYTo.class);

        KeySetPositionFix heuristic = new KeySetPositionFix(createTestIssueDTO(bugId));
        assertNoFixFound(heuristic, program);
    }

    @Test
    public void testFixFound_IfKeyPressed_DifferentKey() throws IOException, ParsingException {
        // Fix: SetX in if(space pressed) - not an arrow key
        String source = """
                when green flag clicked
                if <key (space v) pressed?> then
                    set x to (0)
                end
                """;

        Program program = parseProgram(source);
        String bugId = findBlockId(program, SetXTo.class);

        KeySetPositionFix heuristic = new KeySetPositionFix(createTestIssueDTO(bugId));
        assertFixFound(heuristic, program);
    }

    @Test
    public void testFixFound_BlockDeleted() throws IOException, ParsingException {
        // Bug was fixed by deleting the SetX/SetY block
        String source = """
                when [left arrow v] key pressed
                move (10) steps
                """;

        Program program = parseProgram(source);

        KeySetPositionFix heuristic = new KeySetPositionFix(createTestIssueDTO("non_existent_id"));
        // Deleting the buggy block is a valid fix
        assertFixFound(heuristic, program);
    }

    // ========================================================================
    // INJECTOR-BASED TESTS
    // ========================================================================

    /**
     * Injector-based test for KeySetPosition.
     *
     * <p>The injector converts key-polling motion code (forever + if key pressed + motion)
     * to event-driven code, which introduces the bug (SetX instead of ChangeX).
     * The fix heuristic should detect when the original polling code is correct.
     */
    @Test
    public void testInjector_KeySetPosition() throws IOException, ParsingException {
        // Start with correct polling-style program
        // forever { if (right arrow pressed?) { change x by 10 } }
        String fixedCode = """
                when green flag clicked
                forever
                if <key (right arrow v) pressed?> then
                change x by (10)
                end
                end
                """;

        Program fixedProgram = parseProgram(fixedCode);

        // Verify original has no bug (polling with ChangeX is correct)
        KeySetPosition bugFinder = new KeySetPosition();
        Set<Issue> originalIssues = bugFinder.check(fixedProgram);
        Assertions.assertTrue(originalIssues.isEmpty(),
                "Original polling program with ChangeX should be clean");

        // Inject bug - transforms polling to event-driven with SetX
        KeySetPositionInjector injector = new KeySetPositionInjector();
        List<InjectionResult> results = injector.inject(fixedProgram);

        Assertions.assertFalse(results.isEmpty(),
                "Injector should transform polling to event-driven");

        // Verify bug in mutated program
        Program mutatedProgram = results.getFirst().mutatedProgram();
        Set<Issue> bugIssues = bugFinder.check(mutatedProgram);
        Assertions.assertFalse(bugIssues.isEmpty(),
                "Mutated event-driven program should have KeySetPosition bug");

        // Find the ChangeXBy block ID in the original (correct) program
        String changeXBlockId = findBlockId(fixedProgram, ChangeXBy.class);
        Assertions.assertNotNull(changeXBlockId,
                "Should find ChangeXBy block in original program");

        // Fix heuristic should detect fix on the original program
        KeySetPositionFix fix = new KeySetPositionFix(createTestIssueDTO(changeXBlockId));
        assertFixFound(fix, fixedProgram);
    }
}

