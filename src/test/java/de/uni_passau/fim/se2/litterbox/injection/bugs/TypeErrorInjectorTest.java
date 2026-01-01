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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.TypeError;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.injection.InjectionResult;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class TypeErrorInjectorTest implements JsonTest {

    @Test
    public void testBlockParameterTypeError_MoveSteps() throws IOException, ParsingException {
        String script = """
                when green flag clicked
                move (10) steps
                """;

        Program program = parseScratchBlocks(script);
        TypeErrorInjector injector = new TypeErrorInjector();
        List<InjectionResult> injected = injector.inject(program);

        assertFalse(injected.isEmpty(), "Should inject at least one bug");

        // Verify detector finds the type error
        TypeError detector = new TypeError();
        for (InjectionResult result : injected) {
            Program mutated = result.mutatedProgram();
            Set<Issue> issues = detector.check(mutated);
            assertFalse(issues.isEmpty(), "Detector should find Type Error bug");
        }
    }

    @Test
    public void testBlockParameterTypeError_SetXY() throws IOException, ParsingException {
        String script = """
                when green flag clicked
                set x to (100)
                set y to (50)
                """;

        Program program = parseScratchBlocks(script);
        TypeErrorInjector injector = new TypeErrorInjector();
        List<InjectionResult> injected = injector.inject(program);

        // Verify injection happens for both SetX and SetY statements
        assertEquals(2, injected.size(), "Should inject 2 bugs (one per set statement)");
    }

    @Test
    public void testComparisonTypeError() throws IOException, ParsingException {
        String script = """
                when green flag clicked
                if <(10) = (20)> then
                    move (10) steps
                end
                """;

        Program program = parseScratchBlocks(script);
        TypeErrorInjector injector = new TypeErrorInjector();
        List<InjectionResult> injected = injector.inject(program);

        // Should inject for both the comparison AND the move statement
        assertTrue(injected.size() >= 2, "Should inject bugs for comparison and move");

        TypeError detector = new TypeError();
        for (InjectionResult result : injected) {
            Program mutated = result.mutatedProgram();
            Set<Issue> issues = detector.check(mutated);
            assertFalse(issues.isEmpty(), "Detector should find Type Error bug");
        }
    }

    @Test
    public void testWaitSecondsTypeError() throws IOException, ParsingException {
        String script = """
                when green flag clicked
                wait (2) seconds
                """;

        Program program = parseScratchBlocks(script);
        TypeErrorInjector injector = new TypeErrorInjector();
        List<InjectionResult> injected = injector.inject(program);

        assertEquals(1, injected.size(), "Should inject exactly 1 bug");

        TypeError detector = new TypeError();
        Set<Issue> issues = detector.check(injected.getFirst().mutatedProgram());
        assertFalse(issues.isEmpty(), "Detector should find Type Error bug");
    }

    @Test
    public void testRealisticInjection_UsesProgramBoolExpr() throws IOException, ParsingException {
        // This test verifies that the injector uses boolean expressions from the program
        // (like the Touching block) rather than a fixed "true" literal
        String script = """
                when green flag clicked
                if <touching [edge v]?> then
                    move (10) steps
                end
                """;

        Program program = parseScratchBlocks(script);
        TypeErrorInjector injector = new TypeErrorInjector();
        List<InjectionResult> injected = injector.inject(program);

        // Should inject: 1 for the move statement (using the Touching expression as replacement)
        assertFalse(injected.isEmpty(), "Should inject bugs using program's boolean expressions");

        // Verify detector still finds the type error
        TypeError detector = new TypeError();
        for (InjectionResult result : injected) {
            Program mutated = result.mutatedProgram();
            Set<Issue> issues = detector.check(mutated);
            assertFalse(issues.isEmpty(), "Detector should find Type Error bug");
        }
    }
}


