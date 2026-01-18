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
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.MissingLoopMousePosition;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.export.scratchblocks.ScratchBlocksVisitor;
import de.uni_passau.fim.se2.litterbox.injection.InjectionResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class MissingLoopMousePositionInjectorTest implements JsonTest {

    @Test
    public void testMissingLoopMousePositionInjector() throws IOException, ParsingException {
        // Script with forever loop using mouse x in motion
        String script = """
                when green flag clicked
                forever
                    set x to (mouse x)
                end
                """;

        Program program = parseScratchBlocks(script);

        MissingLoopMousePositionInjector injector = new MissingLoopMousePositionInjector();
        List<InjectionResult> injected = injector.inject(program);

        Assertions.assertFalse(injected.isEmpty(), "Should inject bug");
        Assertions.assertEquals(1, injected.size(), "Should inject exactly 1 bug program");
        Program mutated = injected.getFirst().mutatedProgram();

        // Verification: Check detector
        MissingLoopMousePosition detector = new MissingLoopMousePosition();
        Set<Issue> issues = detector.check(mutated);
        Assertions.assertFalse(issues.isEmpty(), "Detector should find Missing Loop Mouse Position issue");
    }

    @Test
    public void testMissingLoopMousePositionInjectorWithMouseY() throws IOException, ParsingException {
        // Script with forever loop using mouse y in motion
        String script = """
                when green flag clicked
                forever
                    set y to (mouse y)
                end
                """;

        Program program = parseScratchBlocks(script);

        MissingLoopMousePositionInjector injector = new MissingLoopMousePositionInjector();
        List<InjectionResult> injected = injector.inject(program);

        Assertions.assertEquals(1, injected.size(), "Should inject exactly 1 bug program");
        Program mutated = injected.getFirst().mutatedProgram();

        // Verification: Check detector
        MissingLoopMousePosition detector = new MissingLoopMousePosition();
        Set<Issue> issues = detector.check(mutated);
        Assertions.assertFalse(issues.isEmpty(), "Detector should find Missing Loop Mouse Position issue");
    }

    @Test
    public void testMissingLoopMousePositionInjectorWithPointTowardsMouse() throws IOException, ParsingException {
        // Script with forever loop pointing towards mouse
        String script = """
                when green flag clicked
                forever
                    point towards [mouse-pointer]
                end
                """;

        Program program = parseScratchBlocks(script);

        MissingLoopMousePositionInjector injector = new MissingLoopMousePositionInjector();
        List<InjectionResult> injected = injector.inject(program);

        Assertions.assertEquals(1, injected.size(), "Should inject exactly 1 bug program");
        // Note: The detector only checks for MousePos position type, not string-based
        // "mouse-pointer" references, so we don't verify detector here
    }

    @Test
    public void testMultipleStatementsInLoop() throws IOException, ParsingException {
        // Forever loop with multiple statements - all should be unwrapped
        String script = """
                when green flag clicked
                forever
                    set x to (mouse x)
                    set y to (mouse y)
                    say [following mouse]
                end
                """;

        Program program = parseScratchBlocks(script);

        MissingLoopMousePositionInjector injector = new MissingLoopMousePositionInjector();
        List<InjectionResult> injected = injector.inject(program);

        Assertions.assertEquals(1, injected.size(), "Should inject exactly 1 bug program");
        Program mutated = injected.getFirst().mutatedProgram();

        // Verify the loop was unwrapped - statements should be outside the loop now
        String result = ScratchBlocksVisitor.of(mutated);
        Assertions.assertTrue(result.contains("set x to (mouse x)"), "Should have set x statement");
        Assertions.assertTrue(result.contains("set y to (mouse y)"), "Should have set y statement");
        Assertions.assertTrue(result.contains("say [following mouse]"), "Should have say statement");
        Assertions.assertFalse(result.contains("forever"), "Should NOT have forever loop anymore");

        // Verification: Check detector
        MissingLoopMousePosition detector = new MissingLoopMousePosition();
        Set<Issue> issues = detector.check(mutated);
        Assertions.assertFalse(issues.isEmpty(), "Detector should find Missing Loop Mouse Position issue");
    }

    @Test
    public void testRepeatUntilLoop() throws IOException, ParsingException {
        // Repeat-until loop with mouse dependency
        String script = """
                when green flag clicked
                repeat until <touching [edge v] ?>
                    go to x: (mouse x) y: (mouse y)
                end
                """;

        Program program = parseScratchBlocks(script);

        MissingLoopMousePositionInjector injector = new MissingLoopMousePositionInjector();
        List<InjectionResult> injected = injector.inject(program);

        Assertions.assertEquals(1, injected.size(), "Should inject exactly 1 bug program for repeat-until");
        Program mutated = injected.getFirst().mutatedProgram();

        // Verification: Check detector
        MissingLoopMousePosition detector = new MissingLoopMousePosition();
        Set<Issue> issues = detector.check(mutated);
        Assertions.assertFalse(issues.isEmpty(), "Detector should find Missing Loop Mouse Position issue");
    }

    @Test
    public void testNoLoopNoInjection() throws IOException, ParsingException {
        // Script without loop - should not inject
        String script = """
                when green flag clicked
                set x to (mouse x)
                """;

        Program program = parseScratchBlocks(script);

        MissingLoopMousePositionInjector injector = new MissingLoopMousePositionInjector();
        List<InjectionResult> injected = injector.inject(program);

        Assertions.assertTrue(injected.isEmpty(), "Should not inject bug without loop");
    }
}
