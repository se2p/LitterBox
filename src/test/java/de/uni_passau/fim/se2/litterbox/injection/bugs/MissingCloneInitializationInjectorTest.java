package de.uni_passau.fim.se2.litterbox.injection.bugs;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.MissingCloneInitialization;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class MissingCloneInitializationInjectorTest implements JsonTest {

    @Test
    public void testMissingCloneInitializationInjector() throws IOException, ParsingException {
        // Condition: Create Clone + Start as Clone script present
        String script = """
                when green flag clicked
                create clone of (myself v)

                when I start as a clone
                move (10) steps
                """;

        Program program = parseScratchBlocks(script);

        // Inject
        MissingCloneInitializationInjector injector = new MissingCloneInitializationInjector();
        List<Program> injected = injector.inject(program);
        Assertions.assertFalse(injected.isEmpty(), "Should have injected at least one bug");
        Assertions.assertEquals(1, injected.size(), "Should inject exactly 1 bug program");

        // Check
        MissingCloneInitialization detector = new MissingCloneInitialization();

        for (Program mutated : injected) {
            Set<Issue> issues = detector.check(mutated);
            Assertions.assertFalse(issues.isEmpty(), "Detector should find the injected bug (Missing StartAsClone) in all injected programs");
        }
    }

    @Test
    public void testTwoStartAsCloneScripts() throws IOException, ParsingException {
        // Two "when I start as a clone" scripts - both must be removed for the bug
        String script = """
                when green flag clicked
                create clone of (myself v)

                when I start as a clone
                move (10) steps

                when I start as a clone
                turn right (15) degrees
                """;

        Program program = parseScratchBlocks(script);

        // Verify no bug initially
        MissingCloneInitialization detector = new MissingCloneInitialization();
        Set<Issue> initialIssues = detector.check(program);
        Assertions.assertTrue(initialIssues.isEmpty(), "Should have no MissingCloneInitialization bug initially");

        // Inject
        MissingCloneInitializationInjector injector = new MissingCloneInitializationInjector();
        List<Program> injected = injector.inject(program);

        // Should inject exactly 1 bug program (removing BOTH clone handlers)
        Assertions.assertEquals(1, injected.size(), "Should inject exactly 1 bug program");
        Program mutated = injected.getFirst();

        // Verify the detector finds the bug
        Set<Issue> issues = detector.check(mutated);
        Assertions.assertFalse(issues.isEmpty(), "Detector should find MissingCloneInitialization bug");
    }
}
