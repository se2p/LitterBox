package de.uni_passau.fim.se2.litterbox.injection.bugs;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.MissingBackdropSwitch;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class MissingBackdropSwitchInjectorTest implements JsonTest {

    @Test
    public void testMissingBackdropSwitchInjector() throws IOException, ParsingException {
        // Script starting with backdrop switch
        String script = """
                when green flag clicked
                switch backdrop to (backdrop1 v)

                when backdrop switches to [backdrop1 v]
                move (10) steps
                """;

        Program program = parseScratchBlocks(script);

        // Inject
        MissingBackdropSwitchInjector injector = new MissingBackdropSwitchInjector();
        List<Program> injected = injector.inject(program);
        Assertions.assertFalse(injected.isEmpty(), "Should have injected at least one bug");
        Assertions.assertEquals(1, injected.size(), "Should inject exactly 1 bug program");
        Program mutated = injected.getFirst();

        // Verification: Check detector
        MissingBackdropSwitch detector = new MissingBackdropSwitch();
        Set<Issue> issues = detector.check(mutated);
        Assertions.assertFalse(issues.isEmpty(), "Detector should find Missing Backdrop Switch issue");
    }

    @Test
    public void testTwoSwitchesToSameBackdrop() throws IOException, ParsingException {
        // Two switch statements to the same backdrop - both must be removed
        // for the bug to trigger
        String script = """
                when green flag clicked
                switch backdrop to (backdrop1 v)
                wait (1) seconds
                switch backdrop to (backdrop1 v)

                when backdrop switches to [backdrop1 v]
                move (10) steps
                """;

        Program program = parseScratchBlocks(script);

        MissingBackdropSwitchInjector injector = new MissingBackdropSwitchInjector();
        List<Program> injected = injector.inject(program);

        // Should inject exactly 1 bug program, which removes BOTH switches
        Assertions.assertEquals(1, injected.size(), "Should inject exactly 1 bug program");
        Program mutated = injected.getFirst();

        // Verify the detector finds Missing Backdrop Switch bug
        MissingBackdropSwitch detector = new MissingBackdropSwitch();
        Set<Issue> issues = detector.check(mutated);
        Assertions.assertFalse(issues.isEmpty(), "Detector should find Missing Backdrop Switch issue");
    }

    @Test
    public void testNoBackdropHandlerNoInjection() throws IOException, ParsingException {
        // Script with backdrop switch but no handler - nothing to break
        String script = """
                when green flag clicked
                switch backdrop to (backdrop1 v)
                move (10) steps
                """;

        Program program = parseScratchBlocks(script);

        MissingBackdropSwitchInjector injector = new MissingBackdropSwitchInjector();
        List<Program> injected = injector.inject(program);

        // Without a handler for the backdrop switch, no meaningful bug
        Assertions.assertTrue(injected.isEmpty(), "Should not inject without a backdrop switch handler");
    }
}

