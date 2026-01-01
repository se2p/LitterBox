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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.MissingCloneInitialization;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.injection.InjectionResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class MissingCloneInitializationInjectorTest implements JsonTest {

    @Test
    public void testMissingCloneInitializationInjector() throws IOException, ParsingException {
        // Condition: Create Clone + Start as Clone script present
        String script = """
                when green flag clicked
                create clone of (myself v)

                when I start as a clone
                move (10) steps
                """;

        Program program = parseScratchBlocks(script);

        // Inject
        MissingCloneInitializationInjector injector = new MissingCloneInitializationInjector();
        List<InjectionResult> injected = injector.inject(program);
        Assertions.assertFalse(injected.isEmpty(), "Should have injected at least one bug");
        Assertions.assertEquals(1, injected.size(), "Should inject exactly 1 bug program");

        // Check
        MissingCloneInitialization detector = new MissingCloneInitialization();

        for (InjectionResult result : injected) {
            Program mutated = result.mutatedProgram();
            Set<Issue> issues = detector.check(mutated);
            Assertions.assertFalse(issues.isEmpty(), "Detector should find the injected bug (Missing StartAsClone) in all injected programs");
        }
    }

    @Test
    public void testTwoStartAsCloneScripts() throws IOException, ParsingException {
        // Two "when I start as a clone" scripts - both must be removed for the bug
        String script = """
                when green flag clicked
                create clone of (myself v)

                when I start as a clone
                move (10) steps

                when I start as a clone
                turn right (15) degrees
                """;

        Program program = parseScratchBlocks(script);

        // Verify no bug initially
        MissingCloneInitialization detector = new MissingCloneInitialization();
        Set<Issue> initialIssues = detector.check(program);
        Assertions.assertTrue(initialIssues.isEmpty(), "Should have no MissingCloneInitialization bug initially");

        // Inject
        MissingCloneInitializationInjector injector = new MissingCloneInitializationInjector();
        List<InjectionResult> injected = injector.inject(program);

        // Should inject exactly 1 bug program (removing BOTH clone handlers)
        Assertions.assertEquals(1, injected.size(), "Should inject exactly 1 bug program");
        Program mutated = injected.getFirst().mutatedProgram();

        // Verify the detector finds the bug
        Set<Issue> issues = detector.check(mutated);
        Assertions.assertFalse(issues.isEmpty(), "Detector should find MissingCloneInitialization bug");
    }
}
