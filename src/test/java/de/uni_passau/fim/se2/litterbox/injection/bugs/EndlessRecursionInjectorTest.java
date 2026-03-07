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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.EndlessRecursion;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.export.scratchblocks.ScratchBlocksVisitor;
import de.uni_passau.fim.se2.litterbox.injection.InjectionResult;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class EndlessRecursionInjectorTest implements JsonTest {

    @Test
    public void testEndlessRecursionInjector() throws IOException, ParsingException {
        // Procedure called in non-tail position (followed by more code)
        String script = """
                define foo
                move (10) steps

                when green flag clicked
                foo
                say [done]""";

        Program program = parseScratchBlocks(script);

        // Verify original has no issue
        EndlessRecursion detector = new EndlessRecursion();
        assertTrue(detector.check(program).isEmpty(), "Original should have no EndlessRecursion bug");

        // Inject
        EndlessRecursionInjector injector = new EndlessRecursionInjector();
        List<InjectionResult> injected = injector.inject(program);

        assertEquals(1, injected.size(), "Should inject exactly 1 bug program");
        Program mutated = injected.getFirst().mutatedProgram();

        // Verify expected output
        String result = ScratchBlocksVisitor.of(mutated);
        assertTrue(result.contains("define foo" + System.lineSeparator() + "foo"),
                "Recursive call should be inserted at start of procedure. Got:\n" + result);

        // Verify detection
        Set<Issue> issues = detector.check(mutated);
        assertFalse(issues.isEmpty(), "Detector should find EndlessRecursion bug");
    }

    @Test
    public void testNoProcedureNoInjection() throws IOException, ParsingException {
        // Script without any procedures - should not inject
        String script = """
                when green flag clicked
                move (10) steps""";

        Program program = parseScratchBlocks(script);
        EndlessRecursionInjector injector = new EndlessRecursionInjector();
        List<InjectionResult> injected = injector.inject(program);

        assertTrue(injected.isEmpty(), "Should not inject without a procedure definition");
    }

    @Test
    public void testNoInjectionWhenNotCalled() throws IOException, ParsingException {
        // Procedure exists but is never called
        String script = """
                define myProc
                move (10) steps

                when green flag clicked
                say [hello]""";

        Program program = parseScratchBlocks(script);
        EndlessRecursionInjector injector = new EndlessRecursionInjector();
        List<InjectionResult> injected = injector.inject(program);

        assertTrue(injected.isEmpty(), "Should not inject when procedure is not called");
    }

    @Test
    public void testNoInjectionWhenCalledInTailPosition() throws IOException, ParsingException {
        // Procedure called in tail position (last statement, no following code)
        String script = """
                define myProc
                move (10) steps

                when green flag clicked
                say [hello]
                myProc""";

        Program program = parseScratchBlocks(script);
        EndlessRecursionInjector injector = new EndlessRecursionInjector();
        List<InjectionResult> injected = injector.inject(program);

        assertTrue(injected.isEmpty(), "Should not inject when procedure is only called in tail position");
    }
}
