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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.ImmediateStopAfterSayThink;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.injection.InjectionResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class ImmediateStopAfterSayThinkInjectorTest implements JsonTest {

    @Test
    public void testImmediateStopAfterSayInjector() throws IOException, ParsingException {
        // Script with say statement (async version)
        String script = """
                when green flag clicked
                say [Hello!]
                move (10) steps
                """;

        Program program = parseScratchBlocks(script);

        ImmediateStopAfterSayThinkInjector injector = new ImmediateStopAfterSayThinkInjector();
        List<InjectionResult> injected = injector.inject(program);

        Assertions.assertEquals(1, injected.size(), "Should inject exactly 1 bug program");

        // Verification: Check detector finds the issue
        ImmediateStopAfterSayThink detector = new ImmediateStopAfterSayThink();
        for (InjectionResult result : injected) {
            Program mutated = result.mutatedProgram();
            Set<Issue> issues = detector.check(mutated);
            Assertions.assertFalse(issues.isEmpty(), "Detector should find ImmediateStopAfterSay issue");
        }
    }

    @Test
    public void testImmediateStopAfterThinkInjector() throws IOException, ParsingException {
        // Script with think statement (async version)
        String script = """
                when green flag clicked
                think [Hmm...]
                move (10) steps
                """;

        Program program = parseScratchBlocks(script);

        ImmediateStopAfterSayThinkInjector injector = new ImmediateStopAfterSayThinkInjector();
        List<InjectionResult> injected = injector.inject(program);

        Assertions.assertEquals(1, injected.size(), "Should inject exactly 1 bug program");

        // Verification: Check detector finds the issue
        ImmediateStopAfterSayThink detector = new ImmediateStopAfterSayThink();
        for (InjectionResult result : injected) {
            Program mutated = result.mutatedProgram();
            Set<Issue> issues = detector.check(mutated);
            Assertions.assertFalse(issues.isEmpty(), "Detector should find ImmediateStopAfterSay issue for Think");
        }
    }

    @Test
    public void testFallbackSayForSecsConversion() throws IOException, ParsingException {
        // Script with timed say at end (no async version) - should convert and inject
        String script = """
                when green flag clicked
                move (10) steps
                say [Hello!] for (2) secs
                """;

        Program program = parseScratchBlocks(script);

        ImmediateStopAfterSayThinkInjector injector = new ImmediateStopAfterSayThinkInjector();
        List<InjectionResult> injected = injector.inject(program);

        // Should convert SayForSecs at end to Say + StopAll
        Assertions.assertEquals(1, injected.size(), "Should inject via fallback");

        // Verify detection
        ImmediateStopAfterSayThink detector = new ImmediateStopAfterSayThink();
        Assertions.assertFalse(detector.check(injected.getFirst().mutatedProgram()).isEmpty(),
                "Detector should find the bug");
    }

    @Test
    public void testMultipleIdenticalSayStatements() throws IOException, ParsingException {
        // Script with multiple identical say statements - verifies index-based access fix
        String script = """
                when green flag clicked
                say [Hello!]
                move (10) steps
                say [Hello!]
                move (5) steps
                """;

        Program program = parseScratchBlocks(script);

        ImmediateStopAfterSayThinkInjector injector = new ImmediateStopAfterSayThinkInjector();
        List<InjectionResult> injected = injector.inject(program);

        // Should inject 2 mutations (one for each Say statement)
        Assertions.assertEquals(2, injected.size(), "Should inject 2 bug programs (one per Say)");

        // Verify each mutation is detected
        ImmediateStopAfterSayThink detector = new ImmediateStopAfterSayThink();
        for (int i = 0; i < injected.size(); i++) {
            Set<Issue> issues = detector.check(injected.get(i).mutatedProgram());
            Assertions.assertFalse(issues.isEmpty(),
                    "Detector should find ImmediateStopAfterSay issue in mutation " + i);
        }
    }
}

