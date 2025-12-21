package de.uni_passau.fim.se2.litterbox.injection.bugs;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.ImmediateStopAfterSay;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class ImmediateStopAfterSayInjectorTest implements JsonTest {

    @Test
    public void testImmediateStopAfterSayInjector() throws IOException, ParsingException {
        // Script with say statement (async version)
        String script = """
                when green flag clicked
                say [Hello!]
                move (10) steps
                """;

        Program program = parseScratchBlocks(script);

        ImmediateStopAfterSayInjector injector = new ImmediateStopAfterSayInjector();
        List<Program> injected = injector.inject(program);

        Assertions.assertEquals(1, injected.size(), "Should inject exactly 1 bug program");

        // Verification: Check detector finds the issue
        ImmediateStopAfterSay detector = new ImmediateStopAfterSay();
        for (Program mutated : injected) {
            Set<Issue> issues = detector.check(mutated);
            Assertions.assertFalse(issues.isEmpty(), "Detector should find ImmediateStopAfterSay issue");
        }
    }

    @Test
    public void testImmediateStopAfterThinkInjector() throws IOException, ParsingException {
        // Script with think statement (async version)
        String script = """
                when green flag clicked
                think [Hmm...]
                move (10) steps
                """;

        Program program = parseScratchBlocks(script);

        ImmediateStopAfterSayInjector injector = new ImmediateStopAfterSayInjector();
        List<Program> injected = injector.inject(program);

        Assertions.assertEquals(1, injected.size(), "Should inject exactly 1 bug program");

        // Verification: Check detector finds the issue
        ImmediateStopAfterSay detector = new ImmediateStopAfterSay();
        for (Program mutated : injected) {
            Set<Issue> issues = detector.check(mutated);
            Assertions.assertFalse(issues.isEmpty(), "Detector should find ImmediateStopAfterSay issue for Think");
        }
    }
}
