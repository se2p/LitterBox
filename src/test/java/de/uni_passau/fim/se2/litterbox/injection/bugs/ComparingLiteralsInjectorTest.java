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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.ComparingLiterals;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;
import de.uni_passau.fim.se2.litterbox.injection.InjectionResult;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class ComparingLiteralsInjectorTest implements JsonTest {

    @Test
    public void testEqualsWithNumberLiteral() throws IOException, ParsingException {
        // Script with comparison: 1 = x (variable) - number literal on left
        String script = """
                when green flag clicked
                if <(1) = (x)> then
                    move (10) steps
                end
                """;

        Program program = parseScratchBlocks(script);

        // Verify original has no issue
        ComparingLiterals detector = new ComparingLiterals();
        Set<Issue> originalIssues = detector.check(program);
        assertTrue(originalIssues.isEmpty(), "Original should have no ComparingLiterals bug");

        // Inject bug
        ComparingLiteralsInjector injector = new ComparingLiteralsInjector();
        List<InjectionResult> injected = injector.inject(program);

        assertFalse(injected.isEmpty(), "Should inject bug");
        assertEquals(1, injected.size(), "Should inject exactly 1 bug program");
        Program mutated = injected.getFirst().mutatedProgram();

        // Verify expected output: (1) = (x) becomes (1) = (1)
        String result = ScratchBlocksVisitor.of(mutated);
        assertTrue(result.contains("<(1) = (1)>"), "Comparison should be <(1) = (1)>, got: " + result);

        // Verify detection
        Set<Issue> issues = detector.check(mutated);
        assertFalse(issues.isEmpty(), "Detector should find ComparingLiterals bug");
    }

    @Test
    public void testEqualsWithNumberLiteralOnRight() throws IOException, ParsingException {
        // Script with comparison: x = 5 (variable on left, number literal on right)
        String script = """
                when green flag clicked
                if <(x) = (5)> then
                    move (10) steps
                end
                """;

        Program program = parseScratchBlocks(script);

        // Verify original has no issue
        ComparingLiterals detector = new ComparingLiterals();
        assertTrue(detector.check(program).isEmpty(), "Original should have no bug");

        // Inject bug
        ComparingLiteralsInjector injector = new ComparingLiteralsInjector();
        List<InjectionResult> injected = injector.inject(program);

        assertEquals(1, injected.size(), "Should inject exactly 1 bug program");
        Program mutated = injected.getFirst().mutatedProgram();

        // Verify expected output: (x) = (5) becomes (5) = (5)
        String result = ScratchBlocksVisitor.of(mutated);
        assertTrue(result.contains("<(5) = (5)>"), "Comparison should be <(5) = (5)>, got: " + result);

        // Verify detection
        assertFalse(detector.check(mutated).isEmpty(), "Detector should find bug");
    }

    @Test
    public void testLessThanWithNumberLiteral() throws IOException, ParsingException {
        // Script with comparison: 10 < x
        String script = """
                when green flag clicked
                if <(10) < (x)> then
                    move (10) steps
                end
                """;

        Program program = parseScratchBlocks(script);

        // Verify original has no issue
        ComparingLiterals detector = new ComparingLiterals();
        assertTrue(detector.check(program).isEmpty(), "Original should have no bug");

        // Inject bug
        ComparingLiteralsInjector injector = new ComparingLiteralsInjector();
        List<InjectionResult> injected = injector.inject(program);

        assertEquals(1, injected.size(), "Should inject exactly 1 bug program");
        Program mutated = injected.getFirst().mutatedProgram();

        // Verify expected output: (10) < (x) becomes (10) < (10)
        String result = ScratchBlocksVisitor.of(mutated);
        assertTrue(result.contains("<(10) < (10)>"), "Comparison should be <(10) < (10)>, got: " + result);

        // Verify detection
        assertFalse(detector.check(mutated).isEmpty(), "Detector should find bug");
    }

    @Test
    public void testBiggerThanWithNumberLiteral() throws IOException, ParsingException {
        // Script with comparison: x > 100
        String script = """
                when green flag clicked
                if <(x) > (100)> then
                    move (10) steps
                end
                """;

        Program program = parseScratchBlocks(script);

        // Verify original has no issue
        ComparingLiterals detector = new ComparingLiterals();
        assertTrue(detector.check(program).isEmpty(), "Original should have no bug");

        // Inject bug
        ComparingLiteralsInjector injector = new ComparingLiteralsInjector();
        List<InjectionResult> injected = injector.inject(program);

        assertEquals(1, injected.size(), "Should inject exactly 1 bug program");
        Program mutated = injected.getFirst().mutatedProgram();

        // Verify expected output: (x) > (100) becomes (100) > (100)
        String result = ScratchBlocksVisitor.of(mutated);
        assertTrue(result.contains("<(100) > (100)>"), "Comparison should be <(100) > (100)>, got: " + result);

        // Verify detection
        assertFalse(detector.check(mutated).isEmpty(), "Detector should find bug");
    }

    @Test
    public void testEqualsWithStringLiteral() throws IOException, ParsingException {
        // Script with comparison: "hello" = x (string literal on left)
        String script = """
                when green flag clicked
                if <[hello] = (x)> then
                    move (10) steps
                end
                """;

        Program program = parseScratchBlocks(script);

        // Verify original has no issue
        ComparingLiterals detector = new ComparingLiterals();
        assertTrue(detector.check(program).isEmpty(), "Original should have no bug");

        // Inject bug
        ComparingLiteralsInjector injector = new ComparingLiteralsInjector();
        List<InjectionResult> injected = injector.inject(program);

        assertEquals(1, injected.size(), "Should inject exactly 1 bug program");
        Program mutated = injected.getFirst().mutatedProgram();

        // Verify expected output: [hello] = (x) becomes [hello] = [hello]
        String result = ScratchBlocksVisitor.of(mutated);
        assertTrue(result.contains("<[hello] = [hello]>"), "Comparison should be <[hello] = [hello]>, got: " + result);

        // Verify detection
        assertFalse(detector.check(mutated).isEmpty(), "Detector should find bug");
    }

    @Test
    public void testNoInjectionWhenBothLiterals() throws IOException, ParsingException {
        // Script with comparison: 1 = 2 (both already literals)
        String script = """
                when green flag clicked
                if <(1) = (2)> then
                    move (10) steps
                end
                """;

        Program program = parseScratchBlocks(script);

        // Original already has the bug
        ComparingLiterals detector = new ComparingLiterals();
        assertFalse(detector.check(program).isEmpty(), "Original should already have ComparingLiterals bug");

        // Inject - should not create any new mutation since both are already literals
        ComparingLiteralsInjector injector = new ComparingLiteralsInjector();
        List<InjectionResult> injected = injector.inject(program);

        assertTrue(injected.isEmpty(), "Should NOT inject when both operands are already literals");
    }

    @Test
    public void testNoInjectionWhenBothVariables() throws IOException, ParsingException {
        // Script with comparison: x = y (both variables, no literals)
        String script = """
                when green flag clicked
                if <(x) = (y)> then
                    move (10) steps
                end
                """;

        Program program = parseScratchBlocks(script);

        // Verify original has no issue
        ComparingLiterals detector = new ComparingLiterals();
        assertTrue(detector.check(program).isEmpty(), "Original should have no bug");

        // Inject - should not create any mutation since neither is a literal
        ComparingLiteralsInjector injector = new ComparingLiteralsInjector();
        List<InjectionResult> injected = injector.inject(program);

        assertTrue(injected.isEmpty(), "Should NOT inject when neither operand is a literal");
    }

    /**
     * Integration test using a real Scratch project fixture.
     * This verifies the injector works on projects where numbers are stored as strings in JSON.
     */
    @Test
    public void testComparingLiteralsInjectorRealProject() throws IOException, ParsingException {
        // oneVariable.json contains "if <(my variable) = (50)>" which has one literal
        Program program = getAST("src/test/fixtures/questions/oneVariable.json");

        ComparingLiteralsInjector injector = new ComparingLiteralsInjector();
        List<InjectionResult> injected = injector.inject(program);

        // Should be able to inject at least one comparison bug
        assertFalse(injected.isEmpty(),
                "Should inject at least one bug in real project, got: " + injected.size());

        // Verify at least one mutation triggers the detector
        ComparingLiterals detector = new ComparingLiterals();
        boolean foundIssue = false;
        for (InjectionResult result : injected) {
            Program mutated = result.mutatedProgram();
            Set<Issue> issues = detector.check(mutated);
            if (!issues.isEmpty()) {
                foundIssue = true;
                break;
            }
        }
        assertTrue(foundIssue,
                "At least one mutation in real project should trigger the detector");
    }
}

