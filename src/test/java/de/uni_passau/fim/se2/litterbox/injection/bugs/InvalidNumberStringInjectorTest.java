package de.uni_passau.fim.se2.litterbox.injection.bugs;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.InvalidNumberString;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class InvalidNumberStringInjectorTest implements JsonTest {
    @Test
    public void testInvalidNumberStringInjector() throws IOException, ParsingException {
        // Script with addition inside a proper green-flag script context
        String script = """
                when green flag clicked
                say ((1) + (1))
                """;

        Program program = parseScratchBlocks(script);

        InvalidNumberStringInjector injector = new InvalidNumberStringInjector();
        List<Program> injected = injector.inject(program);

        Assertions.assertFalse(injected.isEmpty(), "Should inject bug, got: " + injected.size());

        // Check all mutated programs to see if any trigger the detector
        boolean foundIssue = false;
        for (Program mutated : injected) {
            InvalidNumberString detector = new InvalidNumberString();
            Set<Issue> issues = detector.check(mutated);
            if (!issues.isEmpty()) {
                foundIssue = true;
                break;
            }
        }
        Assertions.assertTrue(foundIssue, "At least one mutation should trigger the detector");
    }
}
