package de.uni_passau.fim.se2.litterbox.injection.bugs;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.BlockingIfElse;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class BlockingIfElseInjectorTest implements JsonTest {

    @Test
    public void testBlockingIfElseInjectorInsideLoop() throws IOException, ParsingException {
        // Condition: Inside loop -> Should inject
        String script = """
                when green flag clicked
                repeat (10)
                    if <(1) = (1)> then
                        move (10) steps
                    else
                        move (20) steps
                    end
                end""";

        Program program = parseScratchBlocks(script);
        BlockingIfElseInjector injector = new BlockingIfElseInjector();
        List<Program> injected = injector.inject(program);

        Assertions.assertEquals(1, injected.size(), "Should inject exactly 1 bug program");

        BlockingIfElse detector = new BlockingIfElse();
        for (Program mutated : injected) {
            Set<Issue> issues = detector.check(mutated);
            Assertions.assertFalse(issues.isEmpty(), "BlockingIfElse detector should find the injected bug in all injected programs");
        }
    }

    @Test
    public void testBlockingIfElseInjectorSkipped() throws IOException, ParsingException {
        // Condition: Last statement, NOT inside loop -> Should skip
        String script = """
                when green flag clicked
                if <(1) = (1)> then
                    move (10) steps
                else
                    move (20) steps
                end""";

        Program program = parseScratchBlocks(script);
        BlockingIfElseInjector injector = new BlockingIfElseInjector();
        List<Program> injected = injector.inject(program);

        Assertions.assertTrue(injected.isEmpty(), "Should NOT inject because it is the last statement and not in loop");
    }

    @Test
    public void testBlockingIfElseInjectorFollowedByCode() throws IOException, ParsingException {
        // Condition: Not inside loop, but followed by code -> Should inject
        String script = """
                when green flag clicked
                if <(1) = (1)> then
                    move (10) steps
                else
                    move (20) steps
                end
                move (30) steps""";

        Program program = parseScratchBlocks(script);
        BlockingIfElseInjector injector = new BlockingIfElseInjector();
        List<Program> injected = injector.inject(program);

        Assertions.assertFalse(injected.isEmpty(), "Should inject because it is followed by code");
        Assertions.assertEquals(1, injected.size(), "Should inject exactly 1 bug program");

        BlockingIfElse detector = new BlockingIfElse();
        for (Program mutated : injected) {
            Set<Issue> issues = detector.check(mutated);
            Assertions.assertFalse(issues.isEmpty(), "Detector should find bug when followed by code in all injected programs");
        }
    }
}
