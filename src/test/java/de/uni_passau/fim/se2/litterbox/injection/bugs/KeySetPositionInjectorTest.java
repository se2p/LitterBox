package de.uni_passau.fim.se2.litterbox.injection.bugs;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.KeySetPosition;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class KeySetPositionInjectorTest implements JsonTest {

    @Test
    public void testSetXToWithRightArrow() throws IOException, ParsingException {
        // Loop-based movement with set x to, triggered by right arrow key
        String script = """
                when green flag clicked
                forever
                    if <key (right arrow v) pressed?> then
                        set x to (100)
                    end
                end
                """;

        Program program = parseScratchBlocks(script);

        // Inject - converts polling pattern to event-driven
        KeySetPositionInjector injector = new KeySetPositionInjector();
        List<Program> injected = injector.inject(program);

        assertEquals(1, injected.size(), "Should produce exactly 1 program");
        Program mutated = injected.getFirst();

        // Verify: extracted to key pressed event
        String result = ScratchBlocksVisitor.of(mutated);
        assertTrue(result.contains("when [right arrow v] key pressed"),
                "Should create a when-key-pressed event for right arrow. Got:\n" + result);
        assertTrue(result.contains("set x to (100)"),
                "Set x to should be in the event script. Got:\n" + result);
        // Original forever loop should be empty (if extracted)
        assertFalse(result.contains("if <key (right arrow v) pressed?> then"),
                "Original if-statement should be removed from forever loop. Got:\n" + result);
    }

    @Test
    public void testSetYToWithUpArrow() throws IOException, ParsingException {
        // Loop-based movement with set y to, triggered by up arrow key
        String script = """
                when green flag clicked
                forever
                    if <key (up arrow v) pressed?> then
                        set y to (100)
                    end
                end
                """;

        Program program = parseScratchBlocks(script);

        KeySetPositionInjector injector = new KeySetPositionInjector();
        List<Program> injected = injector.inject(program);

        assertEquals(1, injected.size());
        Program mutated = injected.getFirst();

        // Verify
        String result = ScratchBlocksVisitor.of(mutated);
        assertTrue(result.contains("when [up arrow v] key pressed"),
                "Should create event for up arrow. Got:\n" + result);
        assertTrue(result.contains("set y to (100)"),
                "Set y to should be in event script. Got:\n" + result);
    }

    @Test
    public void testSetYToWithDownArrow() throws IOException, ParsingException {
        // Loop-based movement with set y to, triggered by down arrow key
        String script = """
                when green flag clicked
                forever
                    if <key (down arrow v) pressed?> then
                        set y to (-100)
                    end
                end
                """;

        Program program = parseScratchBlocks(script);

        KeySetPositionInjector injector = new KeySetPositionInjector();
        List<Program> injected = injector.inject(program);

        assertEquals(1, injected.size());
        Program mutated = injected.getFirst();

        // Verify
        String result = ScratchBlocksVisitor.of(mutated);
        assertTrue(result.contains("when [down arrow v] key pressed"),
                "Should create event for down arrow. Got:\n" + result);
    }

    @Test
    public void testNoChangeWithoutMotion() throws IOException, ParsingException {
        // If-statement without motion - should not trigger extraction
        String script = """
                when green flag clicked
                forever
                    if <key (right arrow v) pressed?> then
                        say [hello]
                    end
                end
                """;

        Program program = parseScratchBlocks(script);

        KeySetPositionInjector injector = new KeySetPositionInjector();
        List<Program> injected = injector.inject(program);

        assertEquals(1, injected.size());
        Program mutated = injected.getFirst();

        // The original pattern should remain unchanged
        String result = ScratchBlocksVisitor.of(mutated);
        assertFalse(result.contains("when [right arrow v] key pressed"),
                "Should NOT create key-pressed event for non-motion if. Got:\n" + result);
        assertTrue(result.contains("forever"),
                "Forever loop should still exist. Got:\n" + result);

        // No bug should be detected (no SET position, just SAY)
        KeySetPosition detector = new KeySetPosition();
        assertTrue(detector.check(mutated).isEmpty(), "Detector should not find bug (no set position)");
    }

    @Test
    public void testNoExtractionWithNonArrowKey() throws IOException, ParsingException {
        // Non-arrow key with motion - should still extract but detector won't flag
        String script = """
                when green flag clicked
                forever
                    if <key (space v) pressed?> then
                        set x to (100)
                    end
                end
                """;

        Program program = parseScratchBlocks(script);

        KeySetPositionInjector injector = new KeySetPositionInjector();
        List<Program> injected = injector.inject(program);

        assertEquals(1, injected.size());
        Program mutated = injected.getFirst();

        // The injector will still extract since it has motion
        String result = ScratchBlocksVisitor.of(mutated);
        assertTrue(result.contains("when [space v] key pressed"),
                "Should still extract with space key. Got:\n" + result);

        // Detector only flags arrow keys, so this is not detected
        KeySetPosition detector = new KeySetPosition();
        assertTrue(detector.check(mutated).isEmpty(), "Detector should NOT flag non-arrow keys");
    }
}
