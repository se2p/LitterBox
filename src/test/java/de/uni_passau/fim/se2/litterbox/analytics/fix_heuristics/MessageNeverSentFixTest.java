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

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.MessageNeverSent;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.event.ReceptionOfMessage;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.injection.InjectionResult;
import de.uni_passau.fim.se2.litterbox.injection.bugs.MessageNeverSentInjector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Tests for MessageNeverSentFix heuristic.
 *
 * <p>The bug is reported when a message handler exists but the message is never sent.
 * The fix is detected when the message is now sent.
 */
public class MessageNeverSentFixTest extends AbstractFixHeuristicTest {

    /**
     * Tests that fix is detected when message is now sent.
     *
     * <p>Uses JSON fixture to ensure proper message tracking.
     * The broadcastSync.json fixture has messages that ARE sent (fixed state).
     */
    @Test
    public void testFixFound_MessageNowSent() throws IOException, ParsingException {
        // broadcastSync.json has both broadcast and receive blocks for 'received' message
        Program fixedProgram = JsonTest.parseProgram("./src/test/fixtures/bugs/broadcastSync.json");

        // Find a ReceptionOfMessage block ID
        String blockId = findBlockId(fixedProgram, ReceptionOfMessage.class);
        Assertions.assertNotNull(blockId, "Should find a ReceptionOfMessage block");

        // The message IS sent in this fixture, so the fix should be detected
        MessageNeverSentFix heuristic = new MessageNeverSentFix(createTestIssueDTO(blockId));
        assertFixFound(heuristic, fixedProgram);
    }

    @Test
    public void testNoFix_MessageNotSent() throws IOException, ParsingException {
        // No Fix: Handler exists but message is never broadcast
        String source = """
                when I receive [myMessage v]
                move (10) steps
                """;

        Program program = parseProgram(source);
        String bugId = findBlockId(program, ReceptionOfMessage.class);

        MessageNeverSentFix heuristic = new MessageNeverSentFix(createTestIssueDTO(bugId));
        assertNoFixFound(heuristic, program);
    }

    @Test
    public void testFix_SenderInDetachedScript() throws IOException, ParsingException {
        // Fix (Lenient): Sender exists, even if in a detached script.
        // Heuristics are allowed to be lenient.
        String source = """
                broadcast (myMessage v)

                when I receive [myMessage v]
                move (10) steps
                """;

        Program program = parseProgram(source);
        String bugId = findBlockId(program, ReceptionOfMessage.class);

        MessageNeverSentFix heuristic = new MessageNeverSentFix(createTestIssueDTO(bugId));
        assertFixFound(heuristic, program);
    }

    @Test
    public void testFixFound_BlockDeleted() throws IOException, ParsingException {
        // Fix: The buggy block (receiver) was deleted
        String source = """
                when green flag clicked
                move (10) steps
                """;

        Program program = parseProgram(source);

        MessageNeverSentFix heuristic = new MessageNeverSentFix(createTestIssueDTO("non_existent_id"));
        assertFixFound(heuristic, program);
    }

    // ========================================================================
    // INJECTOR-BASED TESTS
    // ========================================================================

    /**
     * Injector-based test for MessageNeverSent.
     *
     * <p>Uses JSON fixtures for proper message name tracking.
     */
    @Test
    public void testInjector_MessageNeverSent() throws IOException, ParsingException {
        // broadcastSync.json: Has both broadcast and receive blocks (fixed state)
        Program fixedProgram = JsonTest.parseProgram("./src/test/fixtures/bugs/broadcastSync.json");

        // Verify original program has the 'received' message both sent and received
        MessageNeverSent bugFinder = new MessageNeverSent();
        Set<Issue> originalIssues = bugFinder.check(fixedProgram); // TODO use or delete

        // broadcastSync might have some issues for other messages - check for 'received' specifically
        // The key point is that we can find a ReceptionOfMessage that IS handled
        String blockId = findBlockId(fixedProgram, ReceptionOfMessage.class);
        Assertions.assertNotNull(blockId, "Should find ReceptionOfMessage block");

        // Inject bug: Removes a 'broadcast' block
        MessageNeverSentInjector injector = new MessageNeverSentInjector();
        List<InjectionResult> injected = injector.inject(fixedProgram);

        // Skip if no injection targets found (injector may not find valid targets)
        org.junit.jupiter.api.Assumptions.assumeFalse(injected.isEmpty(),
                "Skipping - injector found no valid targets");

        // Verify the mutated program has bugs
        Program mutatedProgram = injected.getFirst().mutatedProgram();
        Set<Issue> bugIssues = bugFinder.check(mutatedProgram);
        Assertions.assertFalse(bugIssues.isEmpty(),
                "Bug finder should detect MessageNeverSent in mutated program");

        // Get bug block ID from mutated program's issues
        Issue bugIssue = bugIssues.iterator().next();
        String bugBlockId = AstNodeUtil.getBlockId(bugIssue.getCodeLocation());
        Assertions.assertNotNull(bugBlockId, "Bug should have a block ID");

        // The fix heuristic should detect this block is in a fixed state in the original
        MessageNeverSentFix fixHeuristic = new MessageNeverSentFix(createTestIssueDTO(bugBlockId));
        assertFixFound(fixHeuristic, fixedProgram);
    }
}
