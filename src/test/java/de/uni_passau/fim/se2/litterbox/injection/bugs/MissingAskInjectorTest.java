package de.uni_passau.fim.se2.litterbox.injection.bugs;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.MissingAsk;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class MissingAskInjectorTest implements JsonTest {

    @Test
    public void testMissingAskInjector() throws IOException, ParsingException {
        // Script with ask and answer
        String script = """
                when green flag clicked
                ask [What's your name?] and wait
                say (answer)
                """;

        Program program = parseScratchBlocks(script);

        MissingAskInjector injector = new MissingAskInjector();
        List<Program> injected = injector.inject(program);

        Assertions.assertFalse(injected.isEmpty(), "Should inject bug");
        Assertions.assertEquals(1, injected.size(), "Should inject exactly 1 bug program");

        // Verification: Check detector finds the issue
        MissingAsk detector = new MissingAsk();
        for (Program mutated : injected) {
            Set<Issue> issues = detector.check(mutated);
            Assertions.assertFalse(issues.isEmpty(), "Detector should find MissingAsk issue");
        }
    }

    @Test
    public void testMissingAskWithMultipleAnswers() throws IOException, ParsingException {
        // Script with ask and multiple answer usages
        String script = """
                when green flag clicked
                ask [What's your name?] and wait
                say (answer)
                think (answer)
                """;

        Program program = parseScratchBlocks(script);

        MissingAskInjector injector = new MissingAskInjector();
        List<Program> injected = injector.inject(program);

        Assertions.assertFalse(injected.isEmpty(), "Should inject bug");
        Assertions.assertEquals(1, injected.size(), "Should inject exactly 1 bug program");

        // Verification: Check detector finds the issue for all orphaned Answer usages
        MissingAsk detector = new MissingAsk();
        for (Program mutated : injected) {
            Set<Issue> issues = detector.check(mutated);
            Assertions.assertFalse(issues.isEmpty(), "Detector should find MissingAsk issue");
        }
    }
}
