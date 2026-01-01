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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.CallWithoutDefinition;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.injection.InjectionResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class CallWithoutDefinitionInjectorTest implements JsonTest {

    @Test
    public void testCallWithoutDefinitionInjector() throws IOException, ParsingException {
        String script = """
                define my block
                move (10) steps

                when green flag clicked
                my block""";

        Program program = parseScratchBlocks(script);

        // Verify original program has no issue
        CallWithoutDefinition detector = new CallWithoutDefinition();
        Set<Issue> originalIssues = detector.check(program);
        Assertions.assertTrue(originalIssues.isEmpty(), "Original should have no CallWithoutDefinition bug");

        // Inject bug
        CallWithoutDefinitionInjector injector = new CallWithoutDefinitionInjector();
        List<InjectionResult> results = injector.inject(program);

        Assertions.assertEquals(1, results.size(), "Should produce exactly 1 mutation");

        // Verify bug is now detected
        Set<Issue> issues = detector.check(results.getFirst().mutatedProgram());
        assertFalse(issues.isEmpty(), "Detector should find the injected bug");
    }

    @Test
    public void testCallWithoutDefinitionDifferentFromExisting() throws IOException, ParsingException {
        // Define a procedure "my block " (with space) to potentially conflict with a typo of "my block"
        String script = """
                define my block
                move (10) steps

                define my block
                move (10) steps

                when green flag clicked
                my block""";

        Program program = parseScratchBlocks(script);
        CallWithoutDefinitionInjector injector = new CallWithoutDefinitionInjector();
        List<InjectionResult> results = injector.inject(program);

        // Just verify that the injector runs without error and produces a mutation.
        // The collision avoidance logic is internal, but this ensures no infinite loops or crashes if names are similar.
        // And we can check if the injected name exists in the original definitions?
        // Actually, we can't easily access the internal set of the injector, but we can verify the outcome is a bug.

        assertFalse(results.isEmpty());
        CallWithoutDefinition detector = new CallWithoutDefinition();
        for (InjectionResult result : results) {
            Set<Issue> issues = detector.check(result.mutatedProgram());
            assertFalse(issues.isEmpty(), "Detector should find the bug even with similar existing names");
        }
    }
}
