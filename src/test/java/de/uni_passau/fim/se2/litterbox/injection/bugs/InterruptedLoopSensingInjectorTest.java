package de.uni_passau.fim.se2.litterbox.injection.bugs;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.InterruptedLoopSensing;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class InterruptedLoopSensingInjectorTest implements JsonTest {

    @Test
    public void testInterruptedLoopSensingInjector() throws IOException, ParsingException {
        // Script with sensing in the loop condition (until)
        String script = """
                when green flag clicked
                repeat until <key (space v) pressed?>
                  move (10) steps
                end""";

        Program program = parseScratchBlocks(script);

        // Verify original has no issue
        InterruptedLoopSensing detector = new InterruptedLoopSensing();
        assertTrue(detector.check(program).isEmpty(), "Original should have no bug");

        // Inject
        InterruptedLoopSensingInjector injector = new InterruptedLoopSensingInjector();
        List<Program> injected = injector.inject(program);

        assertEquals(1, injected.size(), "Should inject exactly 1 bug program");
        Program mutated = injected.getFirst();

        // Verify: wait should be inserted in the loop
        String result = ScratchBlocksVisitor.of(mutated);
        assertTrue(result.contains("wait (1) seconds"),
                "Wait statement should be inserted in the loop. Got:\n" + result);

        // Verify detection
        Set<Issue> issues = detector.check(mutated);
        assertFalse(issues.isEmpty(), "Detector should find the bug");
    }

    @Test
    public void testIfInsideForeverLoop() throws IOException, ParsingException {
        // Script with if-statement checking sensing inside a forever loop
        String script = """
                when green flag clicked
                forever
                    if <key (space v) pressed?> then
                        move (10) steps
                    end
                end""";

        Program program = parseScratchBlocks(script);

        // Verify original has no issue
        InterruptedLoopSensing detector = new InterruptedLoopSensing();
        assertTrue(detector.check(program).isEmpty(), "Original should have no bug");

        // Inject
        InterruptedLoopSensingInjector injector = new InterruptedLoopSensingInjector();
        List<Program> injected = injector.inject(program);

        assertEquals(1, injected.size(), "Should inject exactly 1 bug program");
        Program mutated = injected.getFirst();

        // Verify: wait should be inserted in the forever loop
        String result = ScratchBlocksVisitor.of(mutated);
        assertTrue(result.contains("wait (1) seconds"),
                "Wait statement should be inserted. Got:\n" + result);

        // Verify detection
        assertFalse(detector.check(mutated).isEmpty(), "Detector should find the bug");
    }

    @Test
    public void testNoInjectionForWaitUntil() throws IOException, ParsingException {
        // Script with wait until inside forever loop - should NOT inject
        // because wait until already blocks properly
        String script = """
                when green flag clicked
                forever
                    wait until <key (space v) pressed?>
                end""";

        Program program = parseScratchBlocks(script);

        InterruptedLoopSensingInjector injector = new InterruptedLoopSensingInjector();
        List<Program> injected = injector.inject(program);

        assertTrue(injected.isEmpty(),
                "Should NOT inject for wait until (it already blocks properly)");
    }

    @Test
    public void testNoInjectionWithoutSensing() throws IOException, ParsingException {
        // Loop without any sensing blocks
        String script = """
                when green flag clicked
                forever
                    move (10) steps
                end""";

        Program program = parseScratchBlocks(script);

        InterruptedLoopSensingInjector injector = new InterruptedLoopSensingInjector();
        List<Program> injected = injector.inject(program);

        assertTrue(injected.isEmpty(), "Should not inject without sensing blocks");
    }
}
