package de.uni_passau.fim.se2.litterbox.injection.bugs;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.MissingTerminationCondition;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class MissingTerminationConditionInjectorTest implements JsonTest {

    @Test
    public void testMissingTerminationConditionInjector() throws IOException, ParsingException {
        String script = """
                when green flag clicked
                repeat until <key [space v] pressed?>
                  move (10) steps
                end
                say [done]""";

        Program program = parseScratchBlocks(script);
        MissingTerminationConditionInjector injector = new MissingTerminationConditionInjector();
        List<Program> injected = injector.inject(program);

        Assertions.assertFalse(injected.isEmpty(), "Should have injected at least one bug");
        Assertions.assertEquals(1, injected.size(), "Should inject exactly 1 bug program");

        MissingTerminationCondition detector = new MissingTerminationCondition();
        for (Program mutated : injected) {
            Set<Issue> issues = detector.check(mutated);
            Assertions.assertFalse(issues.isEmpty(), "MissingTerminationCondition detector should find the injected bug in all injected programs");
        }
    }
}
