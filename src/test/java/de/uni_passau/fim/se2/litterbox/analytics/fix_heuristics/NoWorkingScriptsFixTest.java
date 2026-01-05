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
package de.uni_passau.fim.se2.litterbox.analytics.fix_heuristics;

import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.NoWorkingScripts;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.injection.InjectionResult;
import de.uni_passau.fim.se2.litterbox.injection.bugs.NoWorkingScriptsInjector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Tests for NoWorkingScriptsFix.
 *
 * <p>Bug condition: An actor has BOTH:
 * <ul>
 *   <li>Dead code: script without event handler but with statements</li>
 *   <li>Empty script: event handler with no statements</li>
 * </ul>
 *
 * <p>Fix: Either add statements to the event handler, or remove the dead code.
 */
public class NoWorkingScriptsFixTest extends AbstractFixHeuristicTest {

    @Test
    public void testFixFound_EventScriptNowHasStatements() throws IOException, ParsingException {
        // Fixed: The event handler now has statements (move step added)
        String code = """
                when green flag clicked
                move (10) steps
                """;

        Program program = parseProgram(code);
        // Use any block ID from the script as the bug location
        String blockId = findBlockId(program, de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.MoveSteps.class);

        NoWorkingScriptsFix finder = new NoWorkingScriptsFix(createTestIssueDTO(blockId));
        assertFixFound(finder, program);
    }

    @Test
    public void testFixFound_ScriptDeleted() throws IOException, ParsingException {
        // Fix: Scripts are gone (or actor is clean)
        String code = """
                when green flag clicked
                move (10) steps
                """;

        Program program = parseProgram(code);
        String blockId = "non-existent-id";

        NoWorkingScriptsFix finder = new NoWorkingScriptsFix(createTestIssueDTO(blockId));
        assertFixFound(finder, program);
    }

    // ========================================================================
    // INJECTOR-BASED TESTS
    // ========================================================================

    @Test
    public void testInjector_WorkingScriptsBroken() throws IOException, ParsingException {
        // Fixed: Actor has a valid script (working)
        String fixedCode = """
                when green flag clicked
                move (10) steps
                """;

        Program fixedProgram = parseProgram(fixedCode);

        // Verify original program has no bug
        NoWorkingScripts bugFinder = new NoWorkingScripts();
        Set<Issue> originalIssues = bugFinder.check(fixedProgram);
        Assertions.assertTrue(originalIssues.isEmpty(),
                "Original (fixed) program should have no NoWorkingScripts issue");

        // Inject bug: Spawns a 'Never' script (dead code) and disconnects the existing one (empty script)
        // Note: NoWorkingScriptsInjector ensures the conditions for NoWorkingScripts are met.
        NoWorkingScriptsInjector injector = new NoWorkingScriptsInjector();
        List<InjectionResult> injected = injector.inject(fixedProgram);

        Assertions.assertFalse(injected.isEmpty(), "Injector should create at least one bug");

        // Verify the mutated program has bugs
        Program mutatedProgram = injected.getFirst().mutatedProgram();
        Set<Issue> bugIssues = bugFinder.check(mutatedProgram);
        Assertions.assertFalse(bugIssues.isEmpty(),
                "Bug finder should detect NoWorkingScripts in mutated program");

        // Get the Block ID from the ORIGINAL program.
        // For NoWorkingScriptsFix, we usually target a block in a script.
        String blockId = findBlockId(fixedProgram, de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.MoveSteps.class);
        Assertions.assertNotNull(blockId, "Should find MoveSteps block in original program");

        // The fix heuristic should detect this is fixed in the original program
        NoWorkingScriptsFix fixHeuristic = new NoWorkingScriptsFix(createTestIssueDTO(blockId));
        assertFixFound(fixHeuristic, fixedProgram);
    }
}
