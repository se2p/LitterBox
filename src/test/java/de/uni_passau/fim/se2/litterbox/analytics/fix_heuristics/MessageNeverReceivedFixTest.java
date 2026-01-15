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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.MessageNeverReceived;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.Broadcast;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ParentVisitor;
import de.uni_passau.fim.se2.litterbox.injection.InjectionResult;
import de.uni_passau.fim.se2.litterbox.injection.bugs.MessageNeverReceivedInjector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Tests for MessageNeverReceivedFix heuristic.
 *
 * <p>The bug is reported when a broadcast is sent but never received.
 * The fix is detected when the message is now received.
 */
public class MessageNeverReceivedFixTest extends AbstractFixHeuristicTest {

    @Test
    public void testFixFound_MessageNowReceived() throws IOException, ParsingException {
        // Fix: Broadcast message is now received by a handler
        String source = """
                when green flag clicked
                broadcast (myMessage v)

                when I receive [myMessage v]
                move (10) steps
                """;

        Program program = parseProgram(source);
        String bugId = findBlockId(program, Broadcast.class);

        MessageNeverReceivedFix heuristic = new MessageNeverReceivedFix(createTestIssueDTO(bugId));
        assertFixFound(heuristic, program);
    }

    @Test
    public void testNoFix_MessageNotReceived() throws IOException, ParsingException {
        // No Fix: Broadcast is sent but no handler receives it
        String source = """
                when green flag clicked
                broadcast (myMessage v)
                """;

        Program program = parseProgram(source);
        String bugId = findBlockId(program, Broadcast.class);

        MessageNeverReceivedFix heuristic = new MessageNeverReceivedFix(createTestIssueDTO(bugId));
        assertNoFixFound(heuristic, program);
    }

    @Test
    public void testFixFound_BlockDeleted() throws IOException, ParsingException {
        // Fix: The buggy block was deleted
        String source = """
                when green flag clicked
                move (10) steps
                """;

        Program program = parseProgram(source);

        MessageNeverReceivedFix heuristic = new MessageNeverReceivedFix(createTestIssueDTO("non_existent_id"));
        assertFixFound(heuristic, program);
    }

    @Test
    public void testFixFound_ReceivedByOtherSprite() throws IOException, ParsingException {
        // Fix: Broadcast in Sprite1 received by a handler in Sprite2
        // Use a project that already has Sprite2
        Program base = getAST("src/test/fixtures/smells/raceConditionDifferentActors.json");
        Program program = new de.uni_passau.fim.se2.litterbox.scratchblocks.ScratchBlocksParser()
                .extendProject(base, "Sprite2", "when I receive [hello v]\nmove (10) steps");

        // Also need a broadcast in Sprite1
        program = new de.uni_passau.fim.se2.litterbox.scratchblocks.ScratchBlocksParser()
                .extendProject(program, "Sprite1", "when green flag clicked\nbroadcast (hello v)");

        ParentVisitor.apply(program);

        String bugId = findBlockId(program, Broadcast.class);
        MessageNeverReceivedFix heuristic = new MessageNeverReceivedFix(createTestIssueDTO(bugId));
        assertFixFound(heuristic, program);
    }

    // ========================================================================
    // INJECTOR-BASED TESTS
    // ========================================================================

    @Test
    public void testInjector_BroadcastNeverReceived() throws IOException, ParsingException {
        // Fixed: A broadcast that IS received
        // Why? Because the injector REMOVES the receiver.
        String fixedCode = """
                when green flag clicked
                broadcast (msg1 v)

                when I receive [msg1 v]
                move (10) steps
                """;

        Program fixedProgram = parseProgram(fixedCode);

        // Verify original program has no bug
        MessageNeverReceived bugFinder = new MessageNeverReceived();
        Set<Issue> originalIssues = bugFinder.check(fixedProgram);
        Assertions.assertTrue(originalIssues.isEmpty(),
                "Original (fixed) program should have no MessageNeverReceived issue");

        // Inject bug: Removes the 'when I receive' script
        MessageNeverReceivedInjector injector = new MessageNeverReceivedInjector();
        List<InjectionResult> injected = injector.inject(fixedProgram);

        Assertions.assertFalse(injected.isEmpty(), "Injector should create at least one bug");

        // Verify the mutated program has bugs
        Program mutatedProgram = injected.getFirst().mutatedProgram();
        Set<Issue> bugIssues = bugFinder.check(mutatedProgram);
        Assertions.assertFalse(bugIssues.isEmpty(),
                "Bug finder should detect MessageNeverReceived in mutated program");

        // Get the Broadcast block ID from the ORIGINAL program
        String bugId = findBlockId(fixedProgram, Broadcast.class);
        Assertions.assertNotNull(bugId, "Should find broadcast block in original program");

        // The fix heuristic should detect this block is in a fixed state
        MessageNeverReceivedFix fixHeuristic = new MessageNeverReceivedFix(createTestIssueDTO(bugId));
        assertFixFound(fixHeuristic, fixedProgram);
    }
}
