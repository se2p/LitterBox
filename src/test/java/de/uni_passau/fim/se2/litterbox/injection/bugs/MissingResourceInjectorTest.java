package de.uni_passau.fim.se2.litterbox.injection.bugs;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.MissingResource;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class MissingResourceInjectorTest implements JsonTest {

    @Test
    public void testMissingResourceCostumeInjector() throws IOException, ParsingException {
        // Script with switch costume (assumes costume1 exists in default sprite)
        String script = """
                when green flag clicked
                switch costume to (costume1 v)
                """;

        Program program = parseScratchBlocks(script);

        MissingResourceInjector injector = new MissingResourceInjector();
        List<Program> injected = injector.inject(program);

        Assertions.assertEquals(1, injected.size(), "Should inject exactly 1 bug program");

        // Verification: Check detector finds the issue
        MissingResource detector = new MissingResource();
        for (Program mutated : injected) {
            Set<Issue> issues = detector.check(mutated);
            Assertions.assertFalse(issues.isEmpty(), "Detector should find MissingResource issue");
        }
    }

    @Test
    public void testMissingResourceSoundInjector() throws IOException, ParsingException {
        // Script with start sound
        String script = """
                when green flag clicked
                start sound (pop v)
                """;

        Program program = parseScratchBlocks(script);

        MissingResourceInjector injector = new MissingResourceInjector();
        List<Program> injected = injector.inject(program);

        Assertions.assertEquals(1, injected.size(), "Should inject exactly 1 bug program");

        // Verification: Check detector finds the issue
        MissingResource detector = new MissingResource();
        for (Program mutated : injected) {
            Set<Issue> issues = detector.check(mutated);
            Assertions.assertFalse(issues.isEmpty(), "Detector should find MissingResource issue for sound");
        }
    }
}
