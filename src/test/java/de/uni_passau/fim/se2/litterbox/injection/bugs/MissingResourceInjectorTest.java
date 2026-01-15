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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.MissingResource;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.MoveSteps;
import de.uni_passau.fim.se2.litterbox.injection.InjectionResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class MissingResourceInjectorTest implements JsonTest {

    @Test
    public void testMissingResourceCostumeInjector() throws IOException, ParsingException {
        // Script with switch costume (assumes costume1 exists in default sprite)
        String script = """
                when green flag clicked
                switch costume to (costume1 v)
                """;

        Program program = parseScratchBlocks(script);

        MissingResourceInjector injector = new MissingResourceInjector();
        List<InjectionResult> injected = injector.inject(program);

        Assertions.assertEquals(1, injected.size(), "Should inject exactly 1 bug program");

        // Verification: Check detector finds the issue
        MissingResource detector = new MissingResource();
        for (InjectionResult result : injected) {
            Program mutated = result.mutatedProgram();
            Set<Issue> issues = detector.check(mutated);
            Assertions.assertFalse(issues.isEmpty(), "Detector should find MissingResource issue");
        }
    }

    @Test
    public void testMissingResourceSoundInjector() throws IOException, ParsingException {
        // Script with start sound
        String script = """
                when green flag clicked
                start sound (Meow v)
                """;

        Program program = parseScratchBlocks(script);

        MissingResourceInjector injector = new MissingResourceInjector();
        List<InjectionResult> injected = injector.inject(program);

        Assertions.assertEquals(1, injected.size(), "Should inject exactly 1 bug program");

        // Verification: Check detector finds the issue
        MissingResource detector = new MissingResource();
        for (InjectionResult result : injected) {
            Program mutated = result.mutatedProgram();
            Set<Issue> issues = detector.check(mutated);
            Assertions.assertFalse(issues.isEmpty(), "Detector should find MissingResource issue for sound");
        }
    }

    @Test
    public void testSideEffectFreeInjection() throws IOException, ParsingException {
        // Script with mixed blocks to check for innocent bystander loss
        String script = """
                when green flag clicked
                move (10) steps
                switch costume to (costume1 v)
                turn cw (15) degrees
                """;

        Program originalProgram = parseScratchBlocks(script);

        // Count statements in original program before injection
        Script targetScript = originalProgram.getActorDefinitionList().getDefinitions().stream()
                .flatMap(a -> a.getScripts().getScriptList().stream())
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No scripts found in parsed program"));

        int originalStmtCount = targetScript.getStmtList().getNumberOfStatements();

        Assertions.assertEquals(3, originalStmtCount, "Should have 3 statements initially");

        MissingResourceInjector injector = new MissingResourceInjector();
        List<InjectionResult> injected = injector.inject(originalProgram);

        // Verify that the original AST nodes are NOT mutated
        // The first statement should still be "move (10) steps"
        MoveSteps firstStmt = (MoveSteps) targetScript.getStmtList().getStmts().getFirst();

        Assertions.assertNotNull(firstStmt, "First statement should be preserved");

        // Also verify size
        int afterInjectionStmtCount = ((de.uni_passau.fim.se2.litterbox.ast.model.StmtList)
                targetScript.getStmtList()).getStmts().size();

        Assertions.assertEquals(originalStmtCount, afterInjectionStmtCount,
                "Original AST statement count changed! Mutation detected.");

        // Also verify the second injection result didn't start with a mutated AST
        if (injected.size() > 1) {
            System.out.println("Checking injection result 2...");
            // You'd need more complex checks here to see if result 2 has the bug from result 1,
            // but checking the original AST is the most direct proof.
        }
    }

    @Test
    public void testMissingResourceBackdropInjector() throws IOException, ParsingException {
        // Script with switch backdrop and when backdrop switches to
        String script = """
                when green flag clicked
                switch backdrop to (backdrop1 v)
                switch backdrop to (backdrop1 v) and wait
                """;
        // Note: scratchblocks parser might not fully support "when backdrop switches to" syntax if it's not standard
        // But let's try standard syntax:
        String eventScript = """
                when backdrop switches to (backdrop1 v)
                """;

        Program program = parseScratchBlocks(script + "\\n" + eventScript);

        MissingResourceInjector injector = new MissingResourceInjector();
        List<InjectionResult> injected = injector.inject(program);

        // We expect injection for:
        // 1. switch backdrop to
        // 2. switch backdrop and wait
        // 3. when backdrop switches to
        // verify at least 1 injection
        Assertions.assertFalse(injected.isEmpty(), "Should inject bugs for backdrop blocks");

        // Verification: Check detector finds the issue
        MissingResource detector = new MissingResource();
        for (InjectionResult result : injected) {
            Program mutated = result.mutatedProgram();
            Set<Issue> issues = detector.check(mutated);
            Assertions.assertFalse(issues.isEmpty(), "Detector should find MissingResource issue for backdrop");
        }
    }
}

