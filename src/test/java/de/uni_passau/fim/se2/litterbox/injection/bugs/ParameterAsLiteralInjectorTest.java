package de.uni_passau.fim.se2.litterbox.injection.bugs;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.ParameterAsLiteral;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class ParameterAsLiteralInjectorTest implements JsonTest {

    @Test
    public void testParameterAsLiteralInjector() throws IOException, ParsingException {
        // Procedure definition with a parameter 'myParam' and usage in 'say'
        String script = """
                define myBlock (myParam)
                say (myParam)
                """;

        Program program = parseScratchBlocks(script);

        ParameterAsLiteralInjector injector = new ParameterAsLiteralInjector();
        List<Program> injected = injector.inject(program);
        Assertions.assertEquals(1, injected.size(), "Should inject exactly 1 bug program");
        Program mutated = injected.getFirst();

        // Verify via scratchblocks
        String result = ScratchBlocksVisitor.of(mutated);
        Assertions.assertTrue(result.contains("say [myParam]"),
                "Should have replaced (myParam) with [myParam]. Got:\n" + result);

        // Verify detection
        ParameterAsLiteral detector = new ParameterAsLiteral();
        Set<Issue> issues = detector.check(mutated);
        Assertions.assertFalse(issues.isEmpty(), "Detector should find the injected bug");
    }

    @Test
    public void testSayForSecsWithParameter() throws IOException, ParsingException {
        String script = """
                define myBlock (myParam)
                say (myParam) for (2) seconds
                """;

        Program program = parseScratchBlocks(script);

        ParameterAsLiteralInjector injector = new ParameterAsLiteralInjector();
        List<Program> injected = injector.inject(program);
        Assertions.assertEquals(1, injected.size(), "Should inject for say for secs");
        Program mutated = injected.getFirst();

        String result = ScratchBlocksVisitor.of(mutated);
        Assertions.assertTrue(result.contains("say [myParam]"),
                "Say for secs should have parameter replaced. Got:\n" + result);

        ParameterAsLiteral detector = new ParameterAsLiteral();
        Assertions.assertFalse(detector.check(mutated).isEmpty(), "Detector should find bug");
    }

    @Test
    public void testThinkForSecsWithParameter() throws IOException, ParsingException {
        String script = """
                define myBlock (myParam)
                think (myParam) for (2) seconds
                """;

        Program program = parseScratchBlocks(script);

        ParameterAsLiteralInjector injector = new ParameterAsLiteralInjector();
        List<Program> injected = injector.inject(program);
        Assertions.assertEquals(1, injected.size(), "Should inject for think for secs");
        Program mutated = injected.getFirst();

        String result = ScratchBlocksVisitor.of(mutated);
        Assertions.assertTrue(result.contains("think [myParam]"),
                "Think for secs should have parameter replaced. Got:\n" + result);

        ParameterAsLiteral detector = new ParameterAsLiteral();
        Assertions.assertFalse(detector.check(mutated).isEmpty(), "Detector should find bug");
    }
}
