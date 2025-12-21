package de.uni_passau.fim.se2.litterbox.injection.bugs;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.MessageNeverSent;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
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
        List<Program> injected = injector.inject(program);
        Assertions.assertFalse(injected.isEmpty(), "Should have injected at least one bug");
        Assertions.assertEquals(1, injected.size(), "Should inject exactly 1 bug program");

        MessageNeverSent detector = new MessageNeverSent();
        for (Program programToCheck : injected) {
            Set<Issue> issues = detector.check(programToCheck);
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
        List<Program> injected = injector.inject(program);
        Assertions.assertFalse(injected.isEmpty(), "Should have injected at least one bug");
        // Should still be 1 mutation (one per unique message)
        Assertions.assertEquals(1, injected.size(), "Should inject exactly 1 bug program (all broadcasts for same message changed together)");

        // Check that the bug finder detects the injected bug
        MessageNeverSent detector = new MessageNeverSent();
        for (Program mutated : injected) {
            Set<Issue> issues = detector.check(mutated);
            Assertions.assertFalse(issues.isEmpty(),
                    "MessageNeverSent detector should find the injected bug when ALL broadcast blocks are changed");
        }
    }
}

