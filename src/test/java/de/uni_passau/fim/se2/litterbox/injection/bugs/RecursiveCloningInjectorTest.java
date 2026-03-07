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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.RecursiveCloning;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.export.scratchblocks.ScratchBlocksVisitor;
import de.uni_passau.fim.se2.litterbox.injection.InjectionResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class RecursiveCloningInjectorTest implements JsonTest {

    @Test
    public void testRecursiveCloningInjector() throws IOException, ParsingException {
        String script = """
                when I start as a clone
                move (10) steps
                """;

        Program program = parseScratchBlocks(script);

        // Inject
        RecursiveCloningInjector injector = new RecursiveCloningInjector();
        List<InjectionResult> injected = injector.inject(program);

        Assertions.assertEquals(1, injected.size(), "Should inject exactly 1 bug program");
        Program mutated = injected.getFirst().mutatedProgram();

        // Verify mutation using scratchblocks output
        String result = ScratchBlocksVisitor.of(mutated);

        Assertions.assertTrue(result.contains("when I start as a clone"),
                "Should still have the clone script. Got:\n" + result);
        Assertions.assertTrue(result.contains("create clone of"),
                "Should have injected create clone of. Got:\n" + result);
        Assertions.assertTrue(result.contains("myself"),
                "Clone target should be myself. Got:\n" + result);

        // Verify detection
        RecursiveCloning detector = new RecursiveCloning();
        Set<Issue> issues = detector.check(mutated);
        Assertions.assertFalse(issues.isEmpty(), "Detector should find the injected bug");
    }
}
