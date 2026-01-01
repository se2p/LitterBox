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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.MissingTerminationCondition;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.injection.InjectionResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class MissingTerminationConditionInjectorTest implements JsonTest {

    @Test
    public void testMissingTerminationConditionInjector() throws IOException, ParsingException {
        String script = """
                when green flag clicked
                repeat until <key [space v] pressed?>
                  move (10) steps
                end
                say [done]""";

        Program program = parseScratchBlocks(script);
        MissingTerminationConditionInjector injector = new MissingTerminationConditionInjector();
        List<InjectionResult> injected = injector.inject(program);

        Assertions.assertFalse(injected.isEmpty(), "Should have injected at least one bug");
        Assertions.assertEquals(1, injected.size(), "Should inject exactly 1 bug program");

        MissingTerminationCondition detector = new MissingTerminationCondition();
        for (InjectionResult result : injected) {
            Program mutated = result.mutatedProgram();
            Set<Issue> issues = detector.check(mutated);
            Assertions.assertFalse(issues.isEmpty(), "MissingTerminationCondition detector should find the injected bug in all injected programs");
        }
    }

    @Test
    public void testMultipleIdenticalUntilStmts() throws IOException, ParsingException {
        // Script with multiple identical until statements - verifies index-based access fix
        String script = """
                when green flag clicked
                repeat until <key [space v] pressed?>
                  move (10) steps
                end
                say [middle]
                repeat until <key [space v] pressed?>
                  move (10) steps
                end
                say [done]""";

        Program program = parseScratchBlocks(script);
        MissingTerminationConditionInjector injector = new MissingTerminationConditionInjector();
        List<InjectionResult> injected = injector.inject(program);

        // Should inject 2 mutations (one for each UntilStmt)
        Assertions.assertEquals(2, injected.size(), "Should inject 2 bug programs (one per UntilStmt)");

        // Verify each mutation is detected
        MissingTerminationCondition detector = new MissingTerminationCondition();
        for (int i = 0; i < injected.size(); i++) {
            Set<Issue> issues = detector.check(injected.get(i).mutatedProgram());
            Assertions.assertFalse(issues.isEmpty(),
                    "Detector should find MissingTerminationCondition issue in mutation " + i);
        }
    }
}
