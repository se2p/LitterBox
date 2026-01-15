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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.VariableAsLiteral;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;
import de.uni_passau.fim.se2.litterbox.injection.InjectionResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class VariableAsLiteralInjectorTest implements JsonTest {

    @Test
    public void testVariableAsLiteralInjector() throws IOException, ParsingException {
        // Script with a variable and a usage of that variable in 'say'
        String script = """
                when green flag clicked
                set [myVar v] to "Hello"
                say (myVar)
                """;

        Program program = parseScratchBlocks(script);

        VariableAsLiteralInjector injector = new VariableAsLiteralInjector();
        List<InjectionResult> injected = injector.inject(program);
        Assertions.assertEquals(1, injected.size(), "Should inject exactly 1 bug program");
        Program mutated = injected.getFirst().mutatedProgram();

        // Verify via scratchblocks
        String result = ScratchBlocksVisitor.of(mutated);
        Assertions.assertTrue(result.contains("say ([myVar])"),
                "Should have replaced (myVar) with ([myVar]). Got:\n" + result);

        // Verify detection
        VariableAsLiteral detector = new VariableAsLiteral();
        Set<Issue> issues = detector.check(mutated);
        Assertions.assertFalse(issues.isEmpty(), "Detector should find the injected bug");
    }

    @Test
    public void testSayForSecs() throws IOException, ParsingException {
        String script = """
                when green flag clicked
                set [myVar v] to "Hello"
                say (myVar) for (2) seconds
                """;

        Program program = parseScratchBlocks(script);

        VariableAsLiteralInjector injector = new VariableAsLiteralInjector();
        List<InjectionResult> injected = injector.inject(program);
        Assertions.assertEquals(1, injected.size(), "Should inject for say for secs");
        Program mutated = injected.getFirst().mutatedProgram();

        String result = ScratchBlocksVisitor.of(mutated);
        Assertions.assertTrue(result.contains("say ([myVar])"),
                "Say for secs should have variable replaced. Got:\n" + result);

        VariableAsLiteral detector = new VariableAsLiteral();
        Assertions.assertFalse(detector.check(mutated).isEmpty(), "Detector should find bug");
    }

    @Test
    public void testThinkForSecs() throws IOException, ParsingException {
        String script = """
                when green flag clicked
                set [myVar v] to "Hello"
                think (myVar) for (2) seconds
                """;

        Program program = parseScratchBlocks(script);

        VariableAsLiteralInjector injector = new VariableAsLiteralInjector();
        List<InjectionResult> injected = injector.inject(program);
        Assertions.assertEquals(1, injected.size(), "Should inject for think for secs");
        Program mutated = injected.getFirst().mutatedProgram();

        String result = ScratchBlocksVisitor.of(mutated);
        Assertions.assertTrue(result.contains("think ([myVar])"),
                "Think for secs should have variable replaced. Got:\n" + result);

        VariableAsLiteral detector = new VariableAsLiteral();
        Assertions.assertFalse(detector.check(mutated).isEmpty(), "Detector should find bug");
    }
}
