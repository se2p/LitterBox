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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.HideWithoutShow;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Show;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.injection.InjectionResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class HideWithoutShowInjectorTest implements JsonTest {

    @Test
    public void testHideWithoutShowInjector() throws IOException, ParsingException {
        // Script with show and hide
        String script = """
                when green flag clicked
                show
                move (10) steps
                wait (1) secs
                hide
                """;

        Program program = parseScratchBlocks(script);

        HideWithoutShowInjector injector = new HideWithoutShowInjector();
        List<InjectionResult> injected = injector.inject(program);

        Assertions.assertFalse(injected.isEmpty(), "Should inject bug");
        Assertions.assertEquals(1, injected.size(), "Should inject exactly 1 bug program");
        Program mutated = injected.getFirst().mutatedProgram();

        // Verification: Check detector
        HideWithoutShow detector = new HideWithoutShow();
        Set<Issue> issues = detector.check(mutated);
        Assertions.assertFalse(issues.isEmpty(), "Detector should find Hide Without Show issue");
    }

    @Test
    public void testShowRemoved() throws IOException, ParsingException {
        // Script with show - verify it's removed after injection
        String script = """
                when green flag clicked
                show
                move (10) steps
                hide
                """;

        Program program = parseScratchBlocks(script);

        // Verify original has Show
        Assertions.assertTrue(hasShow(program), "Original should have Show");

        HideWithoutShowInjector injector = new HideWithoutShowInjector();
        List<InjectionResult> injected = injector.inject(program);

        Assertions.assertEquals(1, injected.size(), "Should inject exactly 1 bug program");
        Program mutated = injected.getFirst().mutatedProgram();

        // Verify Show is removed
        Assertions.assertFalse(hasShow(mutated), "Show should be removed after injection");
    }

    @Test
    public void testNoShowNoInjection() throws IOException, ParsingException {
        // Script with only Hide and no Show - should not inject
        String script = """
                when green flag clicked
                move (10) steps
                hide
                """;

        Program program = parseScratchBlocks(script);

        HideWithoutShowInjector injector = new HideWithoutShowInjector();
        List<InjectionResult> injected = injector.inject(program);

        Assertions.assertTrue(injected.isEmpty(), "Should not inject when there is no Show to remove");
    }

    @Test
    public void testNoHideAddsHide() throws IOException, ParsingException {
        // Script with Show but no Hide - injector now adds Hide
        String script = """
                when green flag clicked
                show
                move (10) steps
                """;

        Program program = parseScratchBlocks(script);

        HideWithoutShowInjector injector = new HideWithoutShowInjector();
        List<InjectionResult> injected = injector.inject(program);

        // Now injects by adding Hide and removing Show
        Assertions.assertFalse(injected.isEmpty(), "Should inject with Hide added");

        // Verify detector finds the bug
        HideWithoutShow detector = new HideWithoutShow();
        Assertions.assertFalse(detector.check(injected.getFirst().mutatedProgram()).isEmpty(),
                "Detector should find bug");
    }

    private boolean hasShow(Program program) {
        AtomicBoolean found = new AtomicBoolean(false);
        program.accept(new ScratchVisitor() {
            @Override
            public void visit(Show node) {
                found.set(true);
            }
        });
        return found.get();
    }
}
