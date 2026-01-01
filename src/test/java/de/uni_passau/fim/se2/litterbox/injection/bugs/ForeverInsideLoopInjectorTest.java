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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.ForeverInsideLoop;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;
import de.uni_passau.fim.se2.litterbox.injection.InjectionResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class ForeverInsideLoopInjectorTest implements JsonTest {

    @Test
    public void testForeverInsideLoopInjector() throws IOException, ParsingException {
        String script = """
                when green flag clicked
                repeat (10)
                  move (10) steps
                end
                say [hello]""";

        Program program = parseScratchBlocks(script);
        ForeverInsideLoopInjector injector = new ForeverInsideLoopInjector();
        List<InjectionResult> injected = injector.inject(program);

        Assertions.assertEquals(1, injected.size(), "Should inject exactly 1 bug program");

        ForeverInsideLoop detector = new ForeverInsideLoop();
        for (InjectionResult result : injected) {
            Program mutated = result.mutatedProgram();
            Set<Issue> issues = detector.check(mutated);
            Assertions.assertFalse(issues.isEmpty(), "ForeverInsideLoop detector should find the injected bug in all injected programs");
        }
    }

    @Test
    public void testRepeatForeverInsertedInLoop() throws IOException, ParsingException {
        String script = """
                when green flag clicked
                repeat (10)
                  move (10) steps
                end
                say [done]""";

        Program program = parseScratchBlocks(script);
        ForeverInsideLoopInjector injector = new ForeverInsideLoopInjector();
        List<InjectionResult> injected = injector.inject(program);

        Assertions.assertEquals(1, injected.size(), "Should inject exactly 1 bug program");
        Program mutated = injected.getFirst().mutatedProgram();

        // Verify: forever loop should be inserted inside the repeat loop
        String result = ScratchBlocksVisitor.of(mutated);
        Assertions.assertTrue(result.contains("repeat (10)" + System.lineSeparator() + "forever"),
                "Forever loop should be inserted inside repeat loop. Got:\n" + result);
    }

    @Test
    public void testNoLoopNoInjection() throws IOException, ParsingException {
        // Script without any loop - should not inject
        String script = """
                when green flag clicked
                move (10) steps
                say [hello]""";

        Program program = parseScratchBlocks(script);
        ForeverInsideLoopInjector injector = new ForeverInsideLoopInjector();
        List<InjectionResult> injected = injector.inject(program);

        Assertions.assertTrue(injected.isEmpty(), "Should not inject without a loop");
    }

    @Test
    public void testNoSuccessorNoInjection() throws IOException, ParsingException {
        // Loop is the last statement - injection should not happen (bug would have no effect)
        String script = """
                when green flag clicked
                repeat (10)
                  move (10) steps
                end""";

        Program program = parseScratchBlocks(script);
        ForeverInsideLoopInjector injector = new ForeverInsideLoopInjector();
        List<InjectionResult> injected = injector.inject(program);

        Assertions.assertTrue(injected.isEmpty(), "Should not inject when loop is the last statement (no successor)");
    }
}
