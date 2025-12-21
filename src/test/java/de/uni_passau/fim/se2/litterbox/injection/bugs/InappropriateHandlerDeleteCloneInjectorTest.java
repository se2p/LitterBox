package de.uni_passau.fim.se2.litterbox.injection.bugs;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.InappropriateHandlerDeleteClone;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class InappropriateHandlerDeleteCloneInjectorTest implements JsonTest {

    @Test
    public void testInappropriateHandlerDeleteCloneInjector() throws IOException, ParsingException {
        // Script with GreenFlag, CreateClone, and StartedAsClone (prerequisites)
        String script = """
                when green flag clicked
                create clone of (myself v)
                move (10) steps

                when I start as a clone
                say [I am a clone]
                """;

        Program program = parseScratchBlocks(script);

        // Verify original has no issue
        InappropriateHandlerDeleteClone detector = new InappropriateHandlerDeleteClone();
        assertTrue(detector.check(program).isEmpty(), "Original should have no bug");

        // Inject
        InappropriateHandlerDeleteCloneInjector injector = new InappropriateHandlerDeleteCloneInjector();
        List<Program> injected = injector.inject(program);

        assertEquals(1, injected.size(), "Should inject exactly 1 bug program");
        Program mutated = injected.getFirst();

        // Verify: DeleteClone should be added at the end of GreenFlag script
        String result = ScratchBlocksVisitor.of(mutated);
        assertTrue(result.contains("move (10) steps" + System.lineSeparator() + "delete this clone"),
                "DeleteClone should be inserted at end of GreenFlag script. Got:\n" + result);

        // Verify detection
        Set<Issue> issues = detector.check(mutated);
        assertFalse(issues.isEmpty(), "Detector should find the injected bug");
    }

    @Test
    public void testNoInjectionWithoutCreateClone() throws IOException, ParsingException {
        // Has StartedAsClone but no CreateClone
        String script = """
                when green flag clicked
                move (10) steps

                when I start as a clone
                say [I am a clone]
                """;

        Program program = parseScratchBlocks(script);
        InappropriateHandlerDeleteCloneInjector injector = new InappropriateHandlerDeleteCloneInjector();
        List<Program> injected = injector.inject(program);

        assertTrue(injected.isEmpty(), "Should not inject without CreateClone");
    }

    @Test
    public void testNoInjectionWithoutStartedAsClone() throws IOException, ParsingException {
        // Has CreateClone but no StartedAsClone
        String script = """
                when green flag clicked
                create clone of (myself v)
                move (10) steps
                """;

        Program program = parseScratchBlocks(script);
        InappropriateHandlerDeleteCloneInjector injector = new InappropriateHandlerDeleteCloneInjector();
        List<Program> injected = injector.inject(program);

        assertTrue(injected.isEmpty(), "Should not inject without StartedAsClone");
    }

    @Test
    public void testNoInjectionWithoutBothPrerequisites() throws IOException, ParsingException {
        // No CreateClone and no StartedAsClone
        String script = """
                when green flag clicked
                move (10) steps
                """;

        Program program = parseScratchBlocks(script);
        InappropriateHandlerDeleteCloneInjector injector = new InappropriateHandlerDeleteCloneInjector();
        List<Program> injected = injector.inject(program);

        assertTrue(injected.isEmpty(), "Should not inject without both prerequisites");
    }
}
