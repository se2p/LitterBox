package de.uni_passau.fim.se2.litterbox.injection.bugs;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.MessageNeverReceived;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class MessageNeverReceivedInjectorTest implements JsonTest {

    @Test
    public void testMessageNeverReceivedInjector() throws IOException, ParsingException {
        // 1. Broadcast "msg" and 2. Receive "msg"
        String script = """
                when green flag clicked
                broadcast (msg v)

                when I receive [msg v]
                """;

        Program program = parseScratchBlocks(script);

        // Inject
        MessageNeverReceivedInjector injector = new MessageNeverReceivedInjector();
        List<Program> injected = injector.inject(program);
        Assertions.assertFalse(injected.isEmpty(), "Should have injected at least one bug");
        Assertions.assertEquals(1, injected.size(), "Should inject exactly 1 bug program");

        // Check
        MessageNeverReceived detector = new MessageNeverReceived();
        for (Program mutated : injected) {
            Set<Issue> issues = detector.check(mutated);
            Assertions.assertFalse(issues.isEmpty(), "MessageNeverReceived detector should find the injected bug in all injected programs");
        }
    }

    @Test
    public void testMessageNeverReceivedInjectorWithMultipleReceivers() throws IOException, ParsingException {
        // Broadcast "msg" and TWO scripts that receive "msg"
        // The injector must change BOTH reception blocks for the bug to be detected
        String script = """
                when green flag clicked
                broadcast (msg v)

                when I receive [msg v]
                say [First receiver]

                when I receive [msg v]
                say [Second receiver]
                """;

        Program program = parseScratchBlocks(script);

        // Inject
        MessageNeverReceivedInjector injector = new MessageNeverReceivedInjector();
        List<Program> injected = injector.inject(program);
        Assertions.assertFalse(injected.isEmpty(), "Should have injected at least one bug");
        // Should still be 1 mutation (one per unique message)
        Assertions.assertEquals(1, injected.size(), "Should inject exactly 1 bug program (all receivers for same message changed together)");

        // Check that the bug finder detects the injected bug
        MessageNeverReceived detector = new MessageNeverReceived();
        for (Program mutated : injected) {
            Set<Issue> issues = detector.check(mutated);
            Assertions.assertFalse(issues.isEmpty(),
                    "MessageNeverReceived detector should find the injected bug when ALL reception blocks are changed");
        }
    }
}

