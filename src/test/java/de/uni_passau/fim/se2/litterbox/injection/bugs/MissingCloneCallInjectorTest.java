package de.uni_passau.fim.se2.litterbox.injection.bugs;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.MissingCloneCall;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class MissingCloneCallInjectorTest implements JsonTest {

    @Test
    public void testMissingCloneCallInjector() throws IOException, ParsingException {
        // Script with StartedAsClone handler AND CreateCloneOf call
        String script = """
                when green flag clicked
                create clone of (myself v)

                when I start as a clone
                move (10) steps
                """;

        Program program = parseScratchBlocks(script);

        // Verify no bug initially (clone call exists)
        MissingCloneCall detector = new MissingCloneCall();
        Set<Issue> initialIssues = detector.check(program);
        Assertions.assertTrue(initialIssues.isEmpty(), "Should have no MissingCloneCall bug initially");

        // Inject
        MissingCloneCallInjector injector = new MissingCloneCallInjector();
        List<Program> injected = injector.inject(program);
        Assertions.assertEquals(1, injected.size(), "Should inject exactly 1 bug program");

        // Verify detection on mutated program
        Program mutated = injected.getFirst();
        Set<Issue> issues = detector.check(mutated);
        Assertions.assertFalse(issues.isEmpty(), "Detector should find the injected bug");
    }

    @Test
    public void testTwoCloneCallsRemoved() throws IOException, ParsingException {
        // Two create clone blocks - both must be removed for the bug to trigger
        String script = """
                when green flag clicked
                create clone of (myself v)
                wait (1) seconds
                create clone of (myself v)

                when I start as a clone
                move (10) steps
                """;

        Program program = parseScratchBlocks(script);

        // Verify no bug initially
        MissingCloneCall detector = new MissingCloneCall();
        Set<Issue> initialIssues = detector.check(program);
        Assertions.assertTrue(initialIssues.isEmpty(), "Should have no MissingCloneCall bug initially");

        // Inject
        MissingCloneCallInjector injector = new MissingCloneCallInjector();
        List<Program> injected = injector.inject(program);

        // Should inject exactly 1 bug program (removing BOTH clone calls)
        Assertions.assertEquals(1, injected.size(), "Should inject exactly 1 bug program");
        Program mutated = injected.getFirst();

        // Verify the detector finds the bug
        Set<Issue> issues = detector.check(mutated);
        Assertions.assertFalse(issues.isEmpty(), "Detector should find MissingCloneCall bug");
    }
}
