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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.MissingEraseAll;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.pen.PenClearStmt;
import de.uni_passau.fim.se2.litterbox.injection.InjectionResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class MissingEraseAllInjectorTest implements JsonTest {

    @Test
    public void testMissingEraseAllInjector() throws IOException, ParsingException {
        // Script with erase all and drawing
        String script = """
                when green flag clicked
                erase all
                pen down
                move (10) steps
                """;

        Program program = parseScratchBlocks(script);

        MissingEraseAllInjector injector = new MissingEraseAllInjector();
        List<InjectionResult> injected = injector.inject(program);

        Assertions.assertEquals(1, injected.size(), "Should inject exactly 1 bug program");
        Program mutated = injected.getFirst().mutatedProgram();

        // Verification: Check detector
        MissingEraseAll detector = new MissingEraseAll();
        Set<Issue> issues = detector.check(mutated);
        Assertions.assertFalse(issues.isEmpty(), "Detector should find Missing Erase All issue");
    }

    @Test
    public void testNoEraseAllNoInjection() throws IOException, ParsingException {
        // Script without erase all - nothing to remove
        String script = """
                when green flag clicked
                pen down
                move (10) steps
                """;

        Program program = parseScratchBlocks(script);

        MissingEraseAllInjector injector = new MissingEraseAllInjector();
        List<InjectionResult> injected = injector.inject(program);

        // Even though PenDown exists, no EraseAll to remove
        Assertions.assertTrue(injected.isEmpty(), "Should not inject when there is no erase all to remove");
    }

    @Test
    public void testNoPenDownNoInjection() throws IOException, ParsingException {
        // Script with erase all but no pen down - precondition not met for the bug
        String script = """
                when green flag clicked
                erase all
                move (10) steps
                """;

        Program program = parseScratchBlocks(script);

        MissingEraseAllInjector injector = new MissingEraseAllInjector();
        List<InjectionResult> injected = injector.inject(program);

        // Without PenDown, the bug isn't meaningful
        Assertions.assertTrue(injected.isEmpty(), "Should not inject without pen down (precondition not met)");
    }

    @Test
    public void testMultipleEraseAllBlocksRemoved() throws IOException, ParsingException {
        // Two erase all blocks - both must be removed for the bug
        String script = """
                when green flag clicked
                erase all
                pen down
                move (10) steps
                erase all
                """;

        Program program = parseScratchBlocks(script);

        // Verify original has PenClearStmt
        Assertions.assertTrue(hasPenClear(program), "Original should have erase all");

        MissingEraseAllInjector injector = new MissingEraseAllInjector();
        List<InjectionResult> injected = injector.inject(program);

        // Should inject exactly 1 bug program (removing BOTH erase all blocks)
        Assertions.assertEquals(1, injected.size(), "Should inject exactly 1 bug program");
        Program mutated = injected.getFirst().mutatedProgram();

        // Verify ALL erase all blocks are removed
        Assertions.assertFalse(hasPenClear(mutated), "All erase all blocks should be removed");

        // Verify detector finds the bug
        MissingEraseAll detector = new MissingEraseAll();
        Set<Issue> issues = detector.check(mutated);
        Assertions.assertFalse(issues.isEmpty(), "Detector should find MissingEraseAll bug");
    }

    private boolean hasPenClear(Program program) {
        AtomicBoolean found = new AtomicBoolean(false);
        checkForPenClear(program, found);
        return found.get();
    }

    private void checkForPenClear(ASTNode node, AtomicBoolean found) {
        if (found.get()) {
            return;
        }
        if (node instanceof PenClearStmt) {
            found.set(true);
            return;
        }
        for (var child : node.getChildren()) {
            checkForPenClear(child, found);
        }
    }
}

