package de.uni_passau.fim.se2.litterbox.injection.bugs;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.NoWorkingScripts;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.GreenFlag;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class NoWorkingScriptsInjectorTest implements JsonTest {

    @Test
    public void testNoWorkingScriptsInjector() throws IOException, ParsingException {
        String script = """
                when green flag clicked
                move (10) steps""";

        Program program = parseScratchBlocks(script);
        NoWorkingScriptsInjector injector = new NoWorkingScriptsInjector();
        List<Program> mutated = injector.inject(program);

        assertNotEquals(0, mutated.size(), "Should produce at least one mutation");
        Assertions.assertEquals(1, mutated.size(), "Should produce exactly 1 mutation");

        // Verify bug with detector
        NoWorkingScripts detector = new NoWorkingScripts();
        for (Program p : mutated) {
            Set<Issue> issues = detector.check(p);
            assertFalse(issues.isEmpty(), "Detector should find the bug");
        }
    }

    @Test
    public void testScriptSplit() throws IOException, ParsingException {
        String script = """
                when green flag clicked
                move (10) steps
                say [hello]""";

        Program program = parseScratchBlocks(script);
        NoWorkingScriptsInjector injector = new NoWorkingScriptsInjector();
        List<Program> injected = injector.inject(program);

        Assertions.assertEquals(1, injected.size(), "Should inject exactly 1 bug program");
        Program mutated = injected.get(0);

        // Verify AST structure: Should have an empty GreenFlag and a Never script with body
        boolean foundEmptyGreenFlag = false;
        boolean foundNeverWithBody = false;

        for (var actor : mutated.getActorDefinitionList().getDefinitions()) {
            for (Script s : actor.getScripts().getScriptList()) {
                if (s.getEvent() instanceof GreenFlag && s.getStmtList().getStmts().isEmpty()) {
                    foundEmptyGreenFlag = true;
                }
                if (s.getEvent() instanceof Never && !s.getStmtList().getStmts().isEmpty()) {
                    foundNeverWithBody = true;
                }
            }
        }

        Assertions.assertTrue(foundEmptyGreenFlag, "Should have empty GreenFlag script");
        Assertions.assertTrue(foundNeverWithBody, "Should have Never script with body (detached code)");
    }

    @Test
    public void testEmptyScriptNoInjection() throws IOException, ParsingException {
        // Script with empty body - nothing to detach
        String script = """
                when green flag clicked""";

        Program program = parseScratchBlocks(script);
        NoWorkingScriptsInjector injector = new NoWorkingScriptsInjector();
        List<Program> injected = injector.inject(program);

        Assertions.assertTrue(injected.isEmpty(), "Should not inject when GreenFlag body is already empty");
    }

    @Test
    public void testNoGreenFlagNoInjection() throws IOException, ParsingException {
        // Script without GreenFlag - not applicable
        String script = """
                when this sprite clicked
                move (10) steps""";

        Program program = parseScratchBlocks(script);
        NoWorkingScriptsInjector injector = new NoWorkingScriptsInjector();
        List<Program> injected = injector.inject(program);

        Assertions.assertTrue(injected.isEmpty(), "Should not inject without GreenFlag event");
    }
}

