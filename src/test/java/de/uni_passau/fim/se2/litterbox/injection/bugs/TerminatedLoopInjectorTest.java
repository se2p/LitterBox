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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.TerminatedLoop;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.export.scratchblocks.ScratchBlocksVisitor;
import de.uni_passau.fim.se2.litterbox.injection.InjectionResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class TerminatedLoopInjectorTest implements JsonTest {

    @Test
    public void testTerminatedLoopInjector() throws IOException, ParsingException {
        String script = """
                when green flag clicked
                forever
                  move (10) steps
                end""";

        Program program = parseScratchBlocks(script);
        TerminatedLoopInjector injector = new TerminatedLoopInjector();
        List<InjectionResult> injected = injector.inject(program);

        Assertions.assertEquals(1, injected.size(), "Should inject exactly 1 bug program");
        Program mutated = injected.getFirst().mutatedProgram();

        String result = ScratchBlocksVisitor.of(mutated);

        Assertions.assertTrue(result.contains("forever"),
                "Should still have forever loop. Got:\n" + result);
        Assertions.assertTrue(result.contains("stop [this script v]"),
                "Should have inserted stop this script. Got:\n" + result);

        TerminatedLoop detector = new TerminatedLoop();
        Set<Issue> issues = detector.check(mutated);
        Assertions.assertFalse(issues.isEmpty(), "Detector should find the injected bug");
    }

    @Test
    public void testRepeatTimesLoop() throws IOException, ParsingException {
        String script = """
                when green flag clicked
                repeat (10)
                  move (10) steps
                end""";

        Program program = parseScratchBlocks(script);
        TerminatedLoopInjector injector = new TerminatedLoopInjector();
        List<InjectionResult> injected = injector.inject(program);

        Assertions.assertEquals(1, injected.size(), "Should inject for repeat loop");
        Program mutated = injected.getFirst().mutatedProgram();

        String result = ScratchBlocksVisitor.of(mutated);

        Assertions.assertTrue(result.contains("repeat (10)"),
                "Should still have repeat loop. Got:\n" + result);
        Assertions.assertTrue(result.contains("stop [this script v]"),
                "Should have inserted stop this script. Got:\n" + result);
    }

    @Test
    public void testNoLoopNoInjection() throws IOException, ParsingException {
        String script = """
                when green flag clicked
                move (10) steps
                say [hello]""";

        Program program = parseScratchBlocks(script);
        TerminatedLoopInjector injector = new TerminatedLoopInjector();
        List<InjectionResult> injected = injector.inject(program);

        Assertions.assertTrue(injected.isEmpty(), "Should not inject without a loop");
    }

    @Test
    public void testAlreadyTerminatedLoopNoInjection() throws IOException, ParsingException {
        // Loop already ends with stop - should not inject
        String script = """
                when green flag clicked
                forever
                  move (10) steps
                  stop [this script v]
                end""";

        Program program = parseScratchBlocks(script);
        TerminatedLoopInjector injector = new TerminatedLoopInjector();
        List<InjectionResult> injected = injector.inject(program);

        Assertions.assertTrue(injected.isEmpty(),
                "Should not inject when loop already ends with stop block");
    }
}
