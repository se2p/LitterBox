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
package de.uni_passau.fim.se2.litterbox.injection.bugs;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.MessageNeverSent;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.injection.InjectionResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class MessageNeverSentInjectorTest implements JsonTest {

    @Test
    public void testMessageNeverSentInjector() throws IOException, ParsingException {
        // 1. Broadcast "msg" and 2. Receive "msg"
        String script = """
                when green flag clicked
                broadcast (msg v)

                when I receive [msg v]
                move (10) steps
                """;

        Program program = parseScratchBlocks(script);

        // Inject
        MessageNeverSentInjector injector = new MessageNeverSentInjector();
        List<InjectionResult> injected = injector.inject(program);
        Assertions.assertFalse(injected.isEmpty(), "Should have injected at least one bug");
        Assertions.assertEquals(1, injected.size(), "Should inject exactly 1 bug program");

        MessageNeverSent detector = new MessageNeverSent();
        for (InjectionResult result : injected) {
            Set<Issue> issues = detector.check(result.mutatedProgram());
            Assertions.assertFalse(issues.isEmpty(), "MessageNeverSent detector should find the injected bug in all injected programs");
        }
    }

    @Test
    public void testMessageNeverSentInjectorWithMultipleBroadcasts() throws IOException, ParsingException {
        // TWO broadcast blocks sending "msg" and one receiver
        // The injector must change BOTH broadcast blocks for the bug to be detected
        String script = """
                when green flag clicked
                broadcast (msg v)
                say [First broadcast]
                broadcast (msg v)

                when I receive [msg v]
                move (10) steps
                """;

        Program program = parseScratchBlocks(script);

        // Inject
        MessageNeverSentInjector injector = new MessageNeverSentInjector();
        List<InjectionResult> injected = injector.inject(program);
        Assertions.assertFalse(injected.isEmpty(), "Should have injected at least one bug");
        // Should still be 1 mutation (one per unique message)
        Assertions.assertEquals(1, injected.size(), "Should inject exactly 1 bug program (all broadcasts for same message changed together)");

        // Check that the bug finder detects the injected bug
        MessageNeverSent detector = new MessageNeverSent();
        for (InjectionResult result : injected) {
            Program mutated = result.mutatedProgram();
            Set<Issue> issues = detector.check(mutated);
            Assertions.assertFalse(issues.isEmpty(),
                    "MessageNeverSent detector should find the injected bug when ALL broadcast blocks are changed");
        }
    }

    @Test
    public void testMessageNeverSentInjectorWithDetachedBroadcast() throws IOException, ParsingException {
        // Broadcast "msg" is DETACHED (inside Never event in AST representation of loose blocks)
        // Receiver "msg" is connected.
        // The injector should IGNORE the detached broadcast and produce NO results.
        String script = """
                when green flag clicked

                broadcast (msg v)

                when I receive [msg v]
                move (10) steps
                """;

        // Note: The text parser might attach loose blocks to something or treat them as scripts with Never event.
        // We need to ensure the structure mimics a detached block.
        // In the parser, blocks separated by newlines might be separate scripts.
        // Using 'when green flag clicked' followed by nothing makes an empty script.
        // The 'broadcast' is a separate script. If it has no hat, it's a Script with Never event?
        // Let's verify parser behavior or manually construct invalid program?
        // Actually, the text format usually parses "stacks". If a stack has no hat, it's a loose block.
        // But ScratchParser usually requires valid structure?
        // The snippet above:
        // Script 1: green flag (empty)
        // Script 2: broadcast (msg) -> No Hat.
        // Script 3: receive msg

        Program program = parseScratchBlocks(script);

        // Inject
        MessageNeverSentInjector injector = new MessageNeverSentInjector();
        List<InjectionResult> injected = injector.inject(program);

        // Should be empty because the only broadcaster is loose/detached, so ignored.
        Assertions.assertTrue(injected.isEmpty(), "Should NOT inject bug if the only broadcaster is detached (already buggy/invalid)");
    }
}

