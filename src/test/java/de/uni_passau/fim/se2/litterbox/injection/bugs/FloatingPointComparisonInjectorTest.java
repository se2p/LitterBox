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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.FloatingPointComparison;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;
import de.uni_passau.fim.se2.litterbox.injection.InjectionResult;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class FloatingPointComparisonInjectorTest implements JsonTest {

    @Test
    public void testEqualsWithSafeOperandsReplacedByUnsafe() throws IOException, ParsingException {
        // Script with unsafe operands (timer, mouse x) that can be collected,
        // and a safe Equals comparison that should be mutated
        String script = """
                when green flag clicked
                set [myvar v] to (timer)
                set [myvar v] to (mouse x)
                if <(1) = (1)> then
                    move (10) steps
                end
                """;

        Program program = parseScratchBlocks(script);

        // Verify original has no issue
        FloatingPointComparison detector = new FloatingPointComparison();
        assertTrue(detector.check(program).isEmpty(), "Original should have no FloatingPointComparison bug");

        // Inject bug
        FloatingPointComparisonInjector injector = new FloatingPointComparisonInjector();
        List<InjectionResult> injected = injector.inject(program);

        assertEquals(1, injected.size(), "Should inject exactly 1 bug program");
        Program mutated = injected.getFirst().mutatedProgram();

        // Verify: one operand is replaced with an unsafe one (timer), the other kept as-is
        String result = ScratchBlocksVisitor.of(mutated);
        assertTrue(result.contains("<(timer) = (1)>"),
                "Comparison should have one unsafe operand from the program. Got:\n" + result);

        // Verify detection
        Set<Issue> issues = detector.check(mutated);
        assertFalse(issues.isEmpty(), "Detector should find FloatingPointComparison bug");
    }

    @Test
    public void testLessThanWithUnsafeOperandsConvertedToEquals() throws IOException, ParsingException {
        // Script with LessThan comparison using two unsafe operands
        String script = """
                when green flag clicked
                if <(timer) < (mouse x)> then
                    move (10) steps
                end
                """;

        Program program = parseScratchBlocks(script);

        // Original has no FloatingPointComparison bug (LessThan is safe)
        FloatingPointComparison detector = new FloatingPointComparison();
        assertTrue(detector.check(program).isEmpty(), "Original should have no bug");

        // Inject bug
        FloatingPointComparisonInjector injector = new FloatingPointComparisonInjector();
        List<InjectionResult> injected = injector.inject(program);

        assertEquals(1, injected.size(), "Should inject exactly 1 bug program");
        Program mutated = injected.getFirst().mutatedProgram();

        // Verify: LessThan should be converted to Equals
        String result = ScratchBlocksVisitor.of(mutated);
        assertTrue(result.contains("<(timer) = (mouse x)>"),
                "LessThan should be converted to Equals. Got:\n" + result);

        // Verify detection
        assertFalse(detector.check(mutated).isEmpty(), "Detector should find bug");
    }

    @Test
    public void testBiggerThanWithUnsafeOperandsConvertedToEquals() throws IOException, ParsingException {
        // Script with BiggerThan comparison using two unsafe operands
        String script = """
                when green flag clicked
                if <(x position) > (y position)> then
                    move (10) steps
                end
                """;

        Program program = parseScratchBlocks(script);

        // Original has no FloatingPointComparison bug (BiggerThan is safe)
        FloatingPointComparison detector = new FloatingPointComparison();
        assertTrue(detector.check(program).isEmpty(), "Original should have no bug");

        // Inject bug
        FloatingPointComparisonInjector injector = new FloatingPointComparisonInjector();
        List<InjectionResult> injected = injector.inject(program);

        assertEquals(1, injected.size(), "Should inject exactly 1 bug program");
        Program mutated = injected.getFirst().mutatedProgram();

        // Verify: BiggerThan should be converted to Equals
        String result = ScratchBlocksVisitor.of(mutated);
        assertTrue(result.contains("<(x position) = (y position)>"),
                "BiggerThan should be converted to Equals. Got:\n" + result);

        // Verify detection
        assertFalse(detector.check(mutated).isEmpty(), "Detector should find bug");
    }

    @Test
    public void testInjectionWithSyntheticPositionXWhenNoUnsafeOperands() throws IOException, ParsingException {
        // Script with only safe operands - injector uses synthetic PositionX fallback
        String script = """
                when green flag clicked
                if <(1) = (2)> then
                    move (10) steps
                end
                """;

        Program program = parseScratchBlocks(script);
        FloatingPointComparisonInjector injector = new FloatingPointComparisonInjector();
        List<InjectionResult> injected = injector.inject(program);

        // With fallback, should inject using synthetic PositionX for one operand
        assertEquals(1, injected.size(), "Should inject with synthetic PositionX fallback");

        // Verify the mutation replaces one operand with x position
        String result = ScratchBlocksVisitor.of(injected.getFirst().mutatedProgram());
        assertTrue(result.contains("<(x position) = (2)>"),
                "Comparison should have one synthetic PositionX operand. Got:\n" + result);

        // Verify detection
        FloatingPointComparison detector = new FloatingPointComparison();
        assertFalse(detector.check(injected.getFirst().mutatedProgram()).isEmpty(),
                "Detector should find bug with synthetic PositionX operand");
    }

    @Test
    public void testNoInjectionForSafeLessThan() throws IOException, ParsingException {
        // LessThan with one safe operand - should not convert to Equals
        String script = """
                when green flag clicked
                if <(1) < (timer)> then
                    move (10) steps
                end
                """;

        Program program = parseScratchBlocks(script);
        FloatingPointComparisonInjector injector = new FloatingPointComparisonInjector();
        List<InjectionResult> injected = injector.inject(program);

        // Should inject because one operand is unsafe (timer)
        assertFalse(injected.isEmpty(),
                "Should convert LessThan to Equals when even only one operand is unsafe");
    }
}
