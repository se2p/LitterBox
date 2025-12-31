/*
 * Copyright (C) 2019-2025 LitterBox contributors
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

import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.MoveSteps;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.ChangeXBy;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * Tests for StutteringMovementFix heuristic.
 *
 * <p>The bug is reported when movement blocks respond to key presses outside of a loop.
 * The fix is detected when the movement is now inside a loop with an if(key pressed?) condition.
 */
public class StutteringMovementFixTest extends AbstractFixHeuristicTest {

    @Test
    public void testFixFound_MovementInsideLoop() throws IOException, ParsingException {
        // Fix: Move is inside a forever loop with key press check
        String source = """
                when green flag clicked
                forever
                    if <key (space v) pressed?> then
                        move (10) steps
                    end
                end
                """;

        Program program = parseProgram(source);
        String bugId = findBlockId(program, MoveSteps.class);

        StutteringMovementFix heuristic = new StutteringMovementFix(bugId);
        assertFixFound(heuristic, program);
    }

    @Test
    public void testFixFound_ChangeXInsideRepeatUntil() throws IOException, ParsingException {
        // Fix: Change X is inside repeat until loop with key press check
        String source = """
                when green flag clicked
                repeat until <(x position) > (200)>
                    if <key (right arrow v) pressed?> then
                        change x by (5)
                    end
                end
                """;

        Program program = parseProgram(source);
        String bugId = findBlockId(program, ChangeXBy.class);

        StutteringMovementFix heuristic = new StutteringMovementFix(bugId);
        assertFixFound(heuristic, program);
    }

    @Test
    public void testNoFix_MovementOutsideLoop() throws IOException, ParsingException {
        // No Fix: Move is NOT inside a loop
        String source = """
                when green flag clicked
                if <key (space v) pressed?> then
                    move (10) steps
                end
                """;

        Program program = parseProgram(source);
        String bugId = findBlockId(program, MoveSteps.class);

        StutteringMovementFix heuristic = new StutteringMovementFix(bugId);
        assertNoFixFound(heuristic, program);
    }

    @Test
    public void testFixFound_BlockDeleted() throws IOException, ParsingException {
        // Fix: The buggy block was deleted
        String source = """
                when green flag clicked
                say [Hello]
                """;

        Program program = parseProgram(source);

        StutteringMovementFix heuristic = new StutteringMovementFix("non_existent_id");
        assertFixFound(heuristic, program);
    }

    @Test
    public void testInjector_StutteringMovement() throws IOException, ParsingException {
        // Start with a fixed program - movement inside loop checking key
        String fixedCode = """
                when green flag clicked
                forever
                    if <key (space v) pressed?> then
                        move (10) steps
                    end
                end""";

        Program fixedProgram = parseProgram(fixedCode);

        // Inject bug
        de.uni_passau.fim.se2.litterbox.injection.bugs.StutteringMovementInjector injector = new de.uni_passau.fim.se2.litterbox.injection.bugs.StutteringMovementInjector();
        java.util.List<de.uni_passau.fim.se2.litterbox.injection.InjectionResult> injected = injector.inject(fixedProgram);

        org.junit.jupiter.api.Assertions.assertFalse(injected.isEmpty(), "Injector should create at least one bug");

        // Verify the mutated program has the bug
        Program mutatedProgram = injected.getFirst().mutatedProgram();
        de.uni_passau.fim.se2.litterbox.analytics.smells.StutteringMovement bugFinder = new de.uni_passau.fim.se2.litterbox.analytics.smells.StutteringMovement();
        java.util.Set<de.uni_passau.fim.se2.litterbox.analytics.Issue> bugIssues = bugFinder.check(mutatedProgram);
        org.junit.jupiter.api.Assertions.assertFalse(bugIssues.isEmpty(), "Bug finder should detect StutteringMovement in mutated program");

        // Get the block ID
        de.uni_passau.fim.se2.litterbox.analytics.Issue bugIssue = bugIssues.iterator().next();
        String bugBlockId = de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil.getBlockId(bugIssue.getCodeLocation());

        // The fix heuristic should detect a fix in the original program
        StutteringMovementFix fixHeuristic = new StutteringMovementFix(bugBlockId);
        assertFixFound(fixHeuristic, fixedProgram);
    }
}
