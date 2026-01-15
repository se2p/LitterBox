/*
 * Copyright (C) 2019-2026 LitterBox contributors
 *
 * This file is part of LitterBox.
 *
 * LitterBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * LitterBox is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LitterBox. If not, see <http://www.gnu.org/licenses/>.
 */
package de.uni_passau.fim.se2.litterbox.injection.bugs;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;
import de.uni_passau.fim.se2.litterbox.injection.InjectionResult;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

        // Inject - converts polling pattern to event-driven while keeping original
        KeySetPositionInjector injector = new KeySetPositionInjector();
        List<InjectionResult> injected = injector.inject(program);

        assertEquals(1, injected.size(), "Should produce exactly 1 program");
        Program mutated = injected.getFirst().mutatedProgram();

        // Verify: extracted to key pressed event AND original kept
        String result = ScratchBlocksVisitor.of(mutated);
        assertTrue(result.contains("when [right arrow v] key pressed"),
                "Should create a when-key-pressed event for right arrow. Got:\n" + result);
        assertTrue(result.contains("set x to (100)"),
                "Set x to should be in the event script. Got:\n" + result);
        // Original forever loop should STILL contain the if-statement
        assertTrue(result.contains("if <key (right arrow v) pressed?> then"),
                "Original if-statement should be kept in forever loop. Got:\n" + result);
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
        List<InjectionResult> injected = injector.inject(program);

        assertEquals(1, injected.size());
        Program mutated = injected.getFirst().mutatedProgram();

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
        List<InjectionResult> injected = injector.inject(program);

        assertEquals(1, injected.size());
        Program mutated = injected.getFirst().mutatedProgram();

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
        List<InjectionResult> injected = injector.inject(program);

        // Should produce no mutations (no motion statements)
        assertEquals(0, injected.size(), "Should not produce mutations without motion statements");
    }

    @Test
    public void testNoExtractionWithNonArrowKey() throws IOException, ParsingException {
        // Non-arrow key with motion - should NOT extract because detector only flags arrow keys
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
        List<InjectionResult> injected = injector.inject(program);

        // Should not produce mutations for non-arrow keys to avoid false positives
        assertEquals(0, injected.size(),
                "Should NOT extract with non-arrow key (space) to avoid false positives");
    }
}
