package de.uni_passau.fim.se2.litterbox.injection.bugs;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.ImmediateDeleteCloneAfterBroadcast;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class ImmediateDeleteCloneAfterBroadcastInjectorTest implements JsonTest {

    @Test
    public void testImmediateDeleteCloneAfterBroadcastInjector() throws IOException, ParsingException {
        // Script with CreateCloneOf, StartedAsClone, and Broadcast
        String script = """
                when green flag clicked
                create clone of (myself v)
                broadcast (msg v)
                say [hello]

                when I start as a clone
                move (10) steps
                """;

        Program program = parseScratchBlocks(script);

        // Inject
        ImmediateDeleteCloneAfterBroadcastInjector injector = new ImmediateDeleteCloneAfterBroadcastInjector();
        List<Program> injected = injector.inject(program);
        Assertions.assertFalse(injected.isEmpty(), "Should have injected at least one bug");
        Assertions.assertEquals(1, injected.size(), "Should inject exactly 1 bug program");

        // Check
        ImmediateDeleteCloneAfterBroadcast detector = new ImmediateDeleteCloneAfterBroadcast();
        for (Program mutated : injected) {
            Set<Issue> issues = detector.check(mutated);
            Assertions.assertFalse(issues.isEmpty(), "Detector should find the injected bug in all injected programs");
        }
    }

    @Test
    public void testNoInjectionWithoutClone() throws IOException, ParsingException {
        // Script without CreateCloneOf - should not inject
        String script = """
                when green flag clicked
                broadcast (msg v)
                """;

        Program program = parseScratchBlocks(script);

        ImmediateDeleteCloneAfterBroadcastInjector injector = new ImmediateDeleteCloneAfterBroadcastInjector();
        List<Program> injected = injector.inject(program);
        Assertions.assertTrue(injected.isEmpty(), "Should not inject without CreateCloneOf");
    }
}
