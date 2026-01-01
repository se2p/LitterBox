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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.MissingAsk;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.Answer;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.injection.InjectionResult;
import de.uni_passau.fim.se2.litterbox.injection.bugs.MissingAskInjector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Tests for MissingAskFix.
 * Bug: Answer block is used but Ask and Wait is never called.
 * Fix: Program now uses Ask and Wait.
 */
public class MissingAskFixTest extends AbstractFixHeuristicTest {

    @Test
    public void testFixFound_AskNowUsed() throws IOException, ParsingException {
        // Answer is used and Ask is now present - fixed
        String code = """
                when green flag clicked
                ask [What is your name?] and wait
                say (answer)
                """;

        Program program = parseProgram(code);
        String blockId = findBlockId(program, Answer.class);

        MissingAskFix finder = new MissingAskFix(createTestIssueDTO(blockId));
        assertFixFound(finder, program);
    }

    @Test
    public void testNoFixFound_AskStillMissing() throws IOException, ParsingException {
        // Answer is used but Ask is still missing
        String code = """
                when green flag clicked
                say (answer)
                """;

        Program program = parseProgram(code);
        String blockId = findBlockId(program, Answer.class);

        MissingAskFix finder = new MissingAskFix(createTestIssueDTO(blockId));
        assertNoFixFound(finder, program);
    }

    @Test
    public void testFixFound_ScriptDeleted() throws IOException, ParsingException {
        // Script containing the bug is gone
        String code = """
                when green flag clicked
                move (10) steps
                """;

        Program program = parseProgram(code);
        String blockId = "non-existent-id";

        MissingAskFix finder = new MissingAskFix(createTestIssueDTO(blockId));
        assertFixFound(finder, program);
    }

    // ========================================================================
    // INJECTOR-BASED TESTS
    // ========================================================================

    @Test
    public void testInjector_AskAndAnswer() throws IOException, ParsingException {
        // Start with a fixed program - ask and answer are both used
        String fixedCode = """
                when green flag clicked
                ask [What is your name?] and wait
                say (answer)""";

        Program fixedProgram = parseProgram(fixedCode);

        // Verify original program has no MissingAsk
        MissingAsk bugFinder = new MissingAsk();
        Set<Issue> originalIssues = bugFinder.check(fixedProgram);
        Assertions.assertTrue(originalIssues.isEmpty(),
                "Original (fixed) program should have no MissingAsk issue");

        // Inject bug
        MissingAskInjector injector = new MissingAskInjector();
        List<InjectionResult> injected = injector.inject(fixedProgram);

        Assertions.assertFalse(injected.isEmpty(), "Injector should create at least one bug");

        // Verify the mutated program has the bug
        Program mutatedProgram = injected.getFirst().mutatedProgram();
        Set<Issue> bugIssues = bugFinder.check(mutatedProgram);
        Assertions.assertFalse(bugIssues.isEmpty(),
                "Bug finder should detect MissingAsk in mutated program");

        // Get the block ID
        Issue bugIssue = bugIssues.iterator().next();
        String bugBlockId = AstNodeUtil.getBlockId(bugIssue.getCodeLocation());
        Assertions.assertNotNull(bugBlockId, "Bug issue should have a block ID");

        // The fix heuristic should detect a fix in the original program
        MissingAskFix fixHeuristic = new MissingAskFix(createTestIssueDTO(bugBlockId));
        assertFixFound(fixHeuristic, fixedProgram);
    }
}

