package de.uni_passau.fim.se2.litterbox.injection.bugs;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.ForeverInsideIf;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class ForeverInsideIfInjectorTest implements JsonTest {

    @Test
    public void testForeverInsideIfInjector() throws IOException, ParsingException {
        // Condition: Forever loop not at the end of the script
        String script = """
                when green flag clicked
                if <(1) = (1)> then
                    move (10) steps
                else
                end
                move (10) steps""";

        Program program = parseScratchBlocks(script);

        ForeverInsideIfInjector injector = new ForeverInsideIfInjector();
        List<Program> injected = injector.inject(program);

        Assertions.assertFalse(injected.isEmpty(), "Should have injected at least one bug");
        Assertions.assertEquals(1, injected.size(), "Should inject exactly 1 bug program");

        ForeverInsideIf detector = new ForeverInsideIf();
        for (Program mutated : injected) {
            Set<Issue> issues = detector.check(mutated);
            Assertions.assertFalse(issues.isEmpty(), "ForeverInsideIf detector should find the injected bug in all injected programs");
        }
    }
}
