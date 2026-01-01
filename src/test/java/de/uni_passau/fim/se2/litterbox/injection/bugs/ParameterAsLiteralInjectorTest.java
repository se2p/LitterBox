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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.ParameterAsLiteral;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;
import de.uni_passau.fim.se2.litterbox.injection.InjectionResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class ParameterAsLiteralInjectorTest implements JsonTest {

    @Test
    public void testParameterAsLiteralInjector() throws IOException, ParsingException {
        // Procedure definition with a parameter 'myParam' and usage in 'say'
        String script = """
                define myBlock (myParam)
                say (myParam)
                """;

        Program program = parseScratchBlocks(script);

        ParameterAsLiteralInjector injector = new ParameterAsLiteralInjector();
        // Since ScratchBlocks parser may create shadow literals that trigger the bug detector,
        // we explicitly ignore existing issues to verify the injection logic itself.
        injector.setIgnoreExistingIssues(true);

        List<InjectionResult> injected = injector.inject(program);
        Assertions.assertEquals(1, injected.size(), "Should inject exactly 1 bug program");
        Program mutated = injected.getFirst().mutatedProgram();

        // Verify via scratchblocks
        String result = ScratchBlocksVisitor.of(mutated);
        Assertions.assertTrue(result.contains("say ([myParam])"),
                "Should have replaced (myParam) with ([myParam]). Got:\n" + result);

        // Verify detection
        ParameterAsLiteral detector = new ParameterAsLiteral();
        Set<Issue> issues = detector.check(mutated);
        Assertions.assertFalse(issues.isEmpty(), "Detector should find the injected bug");
    }

    @Test
    public void testSayForSecsWithParameter() throws IOException, ParsingException {
        String script = """
                define myBlock (myParam)
                say (myParam) for (2) seconds
                """;

        Program program = parseScratchBlocks(script);

        ParameterAsLiteralInjector injector = new ParameterAsLiteralInjector();
        injector.setIgnoreExistingIssues(true);

        List<InjectionResult> injected = injector.inject(program);
        Assertions.assertEquals(1, injected.size(), "Should inject for say for secs");
        Program mutated = injected.getFirst().mutatedProgram();

        String result = ScratchBlocksVisitor.of(mutated);
        Assertions.assertTrue(result.contains("say ([myParam])"),
                "Say for secs should have parameter replaced. Got:\n" + result);

        ParameterAsLiteral detector = new ParameterAsLiteral();
        Assertions.assertFalse(detector.check(mutated).isEmpty(), "Detector should find bug");
    }

    @Test
    public void testThinkForSecsWithParameter() throws IOException, ParsingException {
        String script = """
                define myBlock (myParam)
                think (myParam) for (2) seconds
                """;

        Program program = parseScratchBlocks(script);

        ParameterAsLiteralInjector injector = new ParameterAsLiteralInjector();
        injector.setIgnoreExistingIssues(true);

        List<InjectionResult> injected = injector.inject(program);
        Assertions.assertEquals(1, injected.size(), "Should inject for think for secs");
        Program mutated = injected.getFirst().mutatedProgram();

        String result = ScratchBlocksVisitor.of(mutated);
        Assertions.assertTrue(result.contains("think ([myParam])"),
                "Think for secs should have parameter replaced. Got:\n" + result);

        ParameterAsLiteral detector = new ParameterAsLiteral();
        Assertions.assertFalse(detector.check(mutated).isEmpty(), "Detector should find bug");
    }
}
