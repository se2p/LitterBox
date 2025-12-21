/*
 * Copyright (C) 2019-2025 LitterBox contributors
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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.ImmediateStopAfterSay;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.injection.InjectionResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class ImmediateStopAfterSayInjectorTest implements JsonTest {

    @Test
    public void testImmediateStopAfterSayInjector() throws IOException, ParsingException {
        // Script with say statement (async version)
        String script = """
                when green flag clicked
                say [Hello!]
                move (10) steps
                """;

        Program program = parseScratchBlocks(script);

        ImmediateStopAfterSayInjector injector = new ImmediateStopAfterSayInjector();
        List<InjectionResult> injected = injector.inject(program);

        Assertions.assertEquals(1, injected.size(), "Should inject exactly 1 bug program");

        // Verification: Check detector finds the issue
        ImmediateStopAfterSay detector = new ImmediateStopAfterSay();
        for (InjectionResult result : injected) { Program mutated = result.mutatedProgram();
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

        ImmediateStopAfterSayInjector injector = new ImmediateStopAfterSayInjector();
        List<InjectionResult> injected = injector.inject(program);

        Assertions.assertEquals(1, injected.size(), "Should inject exactly 1 bug program");

        // Verification: Check detector finds the issue
        ImmediateStopAfterSay detector = new ImmediateStopAfterSay();
        for (InjectionResult result : injected) { Program mutated = result.mutatedProgram();
            Set<Issue> issues = detector.check(mutated);
            Assertions.assertFalse(issues.isEmpty(), "Detector should find ImmediateStopAfterSay issue for Think");
        }
    }
}
