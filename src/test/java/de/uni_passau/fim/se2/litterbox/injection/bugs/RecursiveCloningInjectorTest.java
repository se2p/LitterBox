package de.uni_passau.fim.se2.litterbox.injection.bugs;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.RecursiveCloning;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class RecursiveCloningInjectorTest implements JsonTest {

    @Test
    public void testRecursiveCloningInjector() throws IOException, ParsingException {
        String script = """
                when I start as a clone
                move (10) steps
                """;

        Program program = parseScratchBlocks(script);

        // Inject
        RecursiveCloningInjector injector = new RecursiveCloningInjector();
        List<Program> injected = injector.inject(program);

        Assertions.assertEquals(1, injected.size(), "Should inject exactly 1 bug program");
        Program mutated = injected.getFirst();

        // Verify mutation using scratchblocks output
        String result = ScratchBlocksVisitor.of(mutated);

        Assertions.assertTrue(result.contains("when I start as a clone"),
                "Should still have the clone script. Got:\n" + result);
        Assertions.assertTrue(result.contains("create clone of"),
                "Should have injected create clone of. Got:\n" + result);
        Assertions.assertTrue(result.contains("myself"),
                "Clone target should be myself. Got:\n" + result);

        // Verify detection
        RecursiveCloning detector = new RecursiveCloning();
        Set<Issue> issues = detector.check(mutated);
        Assertions.assertFalse(issues.isEmpty(), "Detector should find the injected bug");
    }
}
