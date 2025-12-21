package de.uni_passau.fim.se2.litterbox.injection.bugs;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.MissingInitialization;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class MissingInitializationInjectorTest implements JsonTest {

    @Test
    public void testMissingInitializationInjector() throws IOException, ParsingException {
        // Script with initialization
        String script = """
                when green flag clicked
                go to x: (0) y: (0)
                move (10) steps
                """;

        Program program = parseScratchBlocks(script);

        MissingInitializationInjector injector = new MissingInitializationInjector();
        List<Program> injected = injector.inject(program);

        Assertions.assertEquals(1, injected.size(), "Should generate exactly one mutated program");

        // Verify detector finds the bug
        MissingInitialization detector = new MissingInitialization();
        Set<Issue> issues = detector.check(injected.getFirst());
        Assertions.assertFalse(issues.isEmpty(), "Detector should find Missing Initialization issue");
    }

    @Test
    public void testHideShowInitialization() throws IOException, ParsingException {
        String script = """
                when green flag clicked
                hide
                move (10) steps
                """;

        Program program = parseScratchBlocks(script);

        MissingInitializationInjector injector = new MissingInitializationInjector();
        List<Program> injected = injector.inject(program);

        Assertions.assertEquals(1, injected.size(), "Should inject for hide block");
    }

    @Test
    public void testPointInDirectionInitialization() throws IOException, ParsingException {
        String script = """
                when green flag clicked
                point in direction (90)
                move (10) steps
                """;

        Program program = parseScratchBlocks(script);

        MissingInitializationInjector injector = new MissingInitializationInjector();
        List<Program> injected = injector.inject(program);

        Assertions.assertEquals(1, injected.size(), "Should inject for point in direction block");
    }

    @Test
    public void testSwitchCostumeInitialization() throws IOException, ParsingException {
        String script = """
                when green flag clicked
                switch costume to (costume1 v)
                move (10) steps
                """;

        Program program = parseScratchBlocks(script);

        MissingInitializationInjector injector = new MissingInitializationInjector();
        List<Program> injected = injector.inject(program);

        Assertions.assertEquals(1, injected.size(), "Should inject for switch costume block");
    }

    @Test
    public void testSetSizeInitialization() throws IOException, ParsingException {
        String script = """
                when green flag clicked
                set size to (100) %
                move (10) steps
                """;

        Program program = parseScratchBlocks(script);

        MissingInitializationInjector injector = new MissingInitializationInjector();
        List<Program> injected = injector.inject(program);

        Assertions.assertEquals(1, injected.size(), "Should inject for set size block");
    }

    @Test
    public void testSetVolumeInitialization() throws IOException, ParsingException {
        String script = """
                when green flag clicked
                set volume to (100) %
                play sound (meow v) until done
                """;

        Program program = parseScratchBlocks(script);

        MissingInitializationInjector injector = new MissingInitializationInjector();
        List<Program> injected = injector.inject(program);

        Assertions.assertEquals(1, injected.size(), "Should inject for set volume block");
    }

    @Test
    public void testNoInitNoInjection() throws IOException, ParsingException {
        // Script without any initialization blocks
        String script = """
                when green flag clicked
                move (10) steps
                say [hello]
                """;

        Program program = parseScratchBlocks(script);

        MissingInitializationInjector injector = new MissingInitializationInjector();
        List<Program> injected = injector.inject(program);

        Assertions.assertTrue(injected.isEmpty(), "Should not inject without initialization blocks");
    }

    @Test
    public void testMultipleInitializationBlocks() throws IOException, ParsingException {
        // Multiple initialization blocks - each gets its own mutation
        String script = """
                when green flag clicked
                go to x: (0) y: (0)
                point in direction (90)
                hide
                move (10) steps
                """;

        Program program = parseScratchBlocks(script);

        MissingInitializationInjector injector = new MissingInitializationInjector();
        List<Program> injected = injector.inject(program);

        // Should create 3 mutations (one per initialization block)
        Assertions.assertEquals(3, injected.size(), "Should create mutation for each initialization block");
    }
}
