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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.MissingWaitUntilCondition;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.injection.InjectionResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class MissingWaitUntilConditionInjectorTest implements JsonTest {

    @Test
    public void testMissingWaitUntilConditionInjector() throws IOException, ParsingException {
        String script = """
                when green flag clicked
                wait until <key [space v] pressed?>
                say [done]""";

        Program program = parseScratchBlocks(script);
        MissingWaitUntilConditionInjector injector = new MissingWaitUntilConditionInjector();
        List<InjectionResult> injected = injector.inject(program);

        Assertions.assertFalse(injected.isEmpty(), "Should have injected at least one bug");
        Assertions.assertEquals(1, injected.size(), "Should inject exactly 1 bug program");

        MissingWaitUntilCondition detector = new MissingWaitUntilCondition();
        for (InjectionResult result : injected) {
            Program mutated = result.mutatedProgram();
            Set<Issue> issues = detector.check(mutated);
            Assertions.assertFalse(issues.isEmpty(), "MissingWaitUntilCondition detector should find the injected bug");
        }
    }

    @Test
    public void testMultipleWaitUntilStmts() throws IOException, ParsingException {
        // Script with multiple wait until statements - verifies index-based access
        String script = """
                when green flag clicked
                wait until <key [space v] pressed?>
                say [middle]
                wait until <key [a v] pressed?>
                say [done]""";

        Program program = parseScratchBlocks(script);
        MissingWaitUntilConditionInjector injector = new MissingWaitUntilConditionInjector();
        List<InjectionResult> injected = injector.inject(program);

        // Should inject 2 mutations (one for each WaitUntil)
        Assertions.assertEquals(2, injected.size(), "Should inject 2 bug programs (one per WaitUntil)");

        // Verify each mutation is detected
        MissingWaitUntilCondition detector = new MissingWaitUntilCondition();
        for (int i = 0; i < injected.size(); i++) {
            Set<Issue> issues = detector.check(injected.get(i).mutatedProgram());
            Assertions.assertFalse(issues.isEmpty(),
                    "Detector should find MissingWaitUntilCondition issue in mutation " + i);
        }
    }

    @Test
    public void testSkipsAlreadyUnspecified() throws IOException, ParsingException {
        // The finder detects wait until with unspecified condition - we should not inject there
        // We verify the injector produces no mutations for already buggy code
        Program program = getAST("src/test/fixtures/bugs/missingWaitUntilCondition.json");
        
        // First verify original has issues (confirming fixture is correct)
        MissingWaitUntilCondition detector = new MissingWaitUntilCondition();
        Set<Issue> originalIssues = detector.check(program);
        Assertions.assertFalse(originalIssues.isEmpty(), "Fixture should have existing issues");

        // Now run injector - it should only inject for wait untils that are NOT already unspecified
        MissingWaitUntilConditionInjector injector = new MissingWaitUntilConditionInjector();
        List<InjectionResult> injected = injector.inject(program);

        // All injected mutations should have MORE issues than original (or at least one new one)
        for (InjectionResult result : injected) {
            Set<Issue> mutatedIssues = detector.check(result.mutatedProgram());
            Assertions.assertTrue(mutatedIssues.size() > originalIssues.size(),
                    "Mutated program should have more issues than original");
        }
    }
}
