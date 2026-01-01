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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.ParameterOutOfScope;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;
import de.uni_passau.fim.se2.litterbox.injection.InjectionResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class ParameterOutOfScopeInjectorTest implements JsonTest {

    @Test
    public void testMoveStepsReplacement() throws IOException, ParsingException {
        String script = """
                define myBlock (myParam)
                say (myParam)

                when green flag clicked
                move (10) steps
                """;

        Program program = parseScratchBlocks(script);

        ParameterOutOfScopeInjector injector = new ParameterOutOfScopeInjector();
        List<InjectionResult> injected = injector.inject(program);

        Assertions.assertEquals(1, injected.size(), "Should inject exactly 1 bug program");
        Program mutated = injected.getFirst().mutatedProgram();

        String result = ScratchBlocksVisitor.of(mutated);

        Assertions.assertTrue(result.contains("define myBlock"),
                "Procedure definition should remain. Got:\n" + result);

        Assertions.assertTrue(result.contains("move (myParam) steps"),
                "Move steps should have parameter instead of 10. Got:\n" + result);

        ParameterOutOfScope detector = new ParameterOutOfScope();
        Set<Issue> issues = detector.check(mutated);
        Assertions.assertFalse(issues.isEmpty(), "Detector should find Parameter Out Of Scope issue");
    }

    @Test
    public void testEqualsComparisonReplacement() throws IOException, ParsingException {
        String script = """
                define myBlock (myParam)
                say (myParam)

                when green flag clicked
                if <(answer) = [yes]> then
                    say [matched]
                end
                """;

        Program program = parseScratchBlocks(script);

        ParameterOutOfScopeInjector injector = new ParameterOutOfScopeInjector();
        List<InjectionResult> injected = injector.inject(program);

        Assertions.assertEquals(1, injected.size(), "Should inject for Equals comparison");
        Program mutated = injected.getFirst().mutatedProgram();

        String result = ScratchBlocksVisitor.of(mutated);

        // The first operand of the comparison should be replaced with the parameter
        Assertions.assertTrue(result.contains("myParam"),
                "Comparison should have parameter as operand. Got:\n" + result);

        ParameterOutOfScope detector = new ParameterOutOfScope();
        Set<Issue> issues = detector.check(mutated);
        Assertions.assertFalse(issues.isEmpty(), "Detector should find Parameter Out Of Scope issue");
    }

    @Test
    public void testNoReplaceableNodesNoInjection() throws IOException, ParsingException {
        // Procedure with parameter but script has no replaceable nodes
        // (no MoveSteps, no comparisons)
        String script = """
                define myBlock (myParam)
                say (myParam)

                when green flag clicked
                say [hello]
                """;

        Program program = parseScratchBlocks(script);

        ParameterOutOfScopeInjector injector = new ParameterOutOfScopeInjector();
        List<InjectionResult> injected = injector.inject(program);

        Assertions.assertTrue(injected.isEmpty(), "Should not inject without replaceable nodes");
    }

    @Test
    public void testNoProcedureNoInjection() throws IOException, ParsingException {
        String script = """
                when green flag clicked
                move (10) steps
                """;

        Program program = parseScratchBlocks(script);

        ParameterOutOfScopeInjector injector = new ParameterOutOfScopeInjector();
        List<InjectionResult> injected = injector.inject(program);

        Assertions.assertTrue(injected.isEmpty(), "Should not inject without procedure parameters");
    }
}
