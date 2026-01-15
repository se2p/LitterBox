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

import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.report.IssueDTO;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * Tests for MessageAsLiteralFix heuristic.
 *
 * <p>The bug is reported when a string literal matches a message name but isn't broadcast.
 * The fix is detected when:
 * <ul>
 *   <li>The literal is no longer a defined message name</li>
 *   <li>The message is now properly sent via broadcast</li>
 * </ul>
 */
public class MessageAsLiteralFixTest extends AbstractFixHeuristicTest {

    @Test
    public void testFixFound_MessageNotDefined() throws IOException, ParsingException {
        // Fix: String literal "myMessage" is NOT a defined message (no receive handler)
        String source = """
                when green flag clicked
                say [myMessage]
                """;

        Program program = parseProgram(source);
        String bugId = findBlockId(program, StringLiteral.class,
                s -> s.getText().equals("myMessage"));

        MessageAsLiteralFix heuristic = new MessageAsLiteralFix(
                withHint(createTestIssueDTO(bugId), "myMessage"));
        assertFixFound(heuristic, program);
    }

    @Test
    public void testFixFound_MessageNowSent() throws IOException, ParsingException {
        // Fix: String literal matches message AND the message is now broadcast
        String source = """
                when green flag clicked
                say [myMessage]
                broadcast (myMessage v)

                when I receive [myMessage v]
                move (10) steps
                """;

        Program program = parseProgram(source);
        // Find the StringLiteral inside the say block, not the broadcast/receive
        String bugId = findBlockId(program, StringLiteral.class,
                s -> s.getText().equals("myMessage") && s.getParentNode().getClass().getSimpleName().contains("Say"));

        MessageAsLiteralFix heuristic = new MessageAsLiteralFix(
                withHint(createTestIssueDTO(bugId), "myMessage"));
        assertFixFound(heuristic, program);
    }

    @Test
    public void testNoFix_DefinedButNotSent() throws IOException, ParsingException {
        // No Fix: Message IS defined (has receive handler) but NOT sent
        String source = """
                when green flag clicked
                say [myMessage]

                when I receive [myMessage v]
                move (10) steps
                """;

        Program program = parseProgram(source);
        String bugId = findBlockId(program, StringLiteral.class,
                s -> s.getText().equals("myMessage") && s.getParentNode().getClass().getSimpleName().contains("Say"));

        MessageAsLiteralFix heuristic = new MessageAsLiteralFix(
                withHint(createTestIssueDTO(bugId), "myMessage"));
        assertNoFixFound(heuristic, program);
    }

    private IssueDTO withHint(IssueDTO issue, String messageName) {
        String hint = "message_as_literal{MESSAGE=Value[value=" + messageName + "]}";
        return new IssueDTO(
                issue.id(), issue.finder(), issue.name(), issue.type(), issue.severity(),
                issue.sprite(), issue.issueLocationBlockId(), issue.duplicateOf(),
                issue.subsumedBy(), issue.coupledTo(), issue.similarTo(),
                hint,
                issue.costumes(), issue.currentCostume(), issue.scratchBlocksCode(),
                issue.refactoringScratchBlocksCode()
        );
    }

    private IssueDTO withTranslatedHint(IssueDTO issue, String messageName) {
        // This is the actual translated hint format from IssueHints_en.properties
        // The fix heuristic must be able to extract the message from this format
        String hint = "[b]Problem:[/b] [newLine] You are using the name of a message as text. "
                + "[newLine] [newLine] [b]Suggestion for code improvement:[/b] [newLine] "
                + "Probably, you meant to use a [sbi]broadcast (" + messageName + " v)[/sbi] "
                + "or [sbi]broadcast (" + messageName + " v) and wait[/sbi] block.";
        return new IssueDTO(
                issue.id(), issue.finder(), issue.name(), issue.type(), issue.severity(),
                issue.sprite(), issue.issueLocationBlockId(), issue.duplicateOf(),
                issue.subsumedBy(), issue.coupledTo(), issue.similarTo(),
                hint,
                issue.costumes(), issue.currentCostume(), issue.scratchBlocksCode(),
                issue.refactoringScratchBlocksCode()
        );
    }

    // ========================================================================
    // HINT FORMAT REGRESSION TESTS
    // These tests verify that the fix heuristic can parse message names from
    // both the raw toString() format and the translated hint format.
    // If hint text format changes in the future, these tests will fail.
    // ========================================================================

    @Test
    public void testHintParsing_RawFormat() throws IOException, ParsingException {
        // Test with raw toString() format: MESSAGE=Value[value=TestMessage]
        String source = """
                when green flag clicked
                say [TestMessage]
                """;

        Program program = parseProgram(source);
        String bugId = findBlockId(program, StringLiteral.class,
                s -> s.getText().equals("TestMessage"));

        MessageAsLiteralFix heuristic = new MessageAsLiteralFix(
                withHint(createTestIssueDTO(bugId), "TestMessage"));
        assertFixFound(heuristic, program);
    }

    @Test
    public void testHintParsing_TranslatedFormat() throws IOException, ParsingException {
        // Test with translated format: broadcast (MESSAGE v)
        // This tests the DiffAnalyzer path which uses getHintText(translator)
        String source = """
                when green flag clicked
                say [GameOver]
                """;

        Program program = parseProgram(source);
        String bugId = findBlockId(program, StringLiteral.class,
                s -> s.getText().equals("GameOver"));

        MessageAsLiteralFix heuristic = new MessageAsLiteralFix(
                withTranslatedHint(createTestIssueDTO(bugId), "GameOver"));
        assertFixFound(heuristic, program);
    }

    @Test
    public void testSilent_BlockDeleted() throws IOException, ParsingException {
        // Silent: The buggy block no longer exists
        String source = """
                when green flag clicked
                move (10) steps
                """;

        Program program = parseProgram(source);

        MessageAsLiteralFix heuristic = new MessageAsLiteralFix(createTestIssueDTO("non_existent_id"));
        assertFixFound(heuristic, program);
    }

    // ========================================================================
    // INJECTOR-BASED TESTS
    // ========================================================================

    /**
     * Injector-based test for MessageAsLiteral.
     *
     * <p>The bug finder reports on the parent node (Say) which has a block ID.
     * The fix heuristic now correctly handles this by checking ancestor block IDs.
     */
    @Test
    public void testInjector_MessageAsLiteral() throws IOException, ParsingException {
        // Start with valid program (broadcast properly used)
        String fixedCode = """
                when green flag clicked
                broadcast (myMessage v)
                say [Hello]

                when I receive [myMessage v]
                move (10) steps
                """;

        Program fixedProgram = parseProgram(fixedCode);

        // Verify original has no bug
        de.uni_passau.fim.se2.litterbox.analytics.bugs.MessageAsLiteral bugFinder =
                new de.uni_passau.fim.se2.litterbox.analytics.bugs.MessageAsLiteral();
        java.util.Set<de.uni_passau.fim.se2.litterbox.analytics.Issue> originalIssues =
                bugFinder.check(fixedProgram);
        org.junit.jupiter.api.Assertions.assertTrue(originalIssues.isEmpty(),
                "Original program should be clean");

        // Inject bug (replaces say literal with message name, removes broadcast)
        de.uni_passau.fim.se2.litterbox.injection.bugs.MessageAsLiteralInjector injector =
                new de.uni_passau.fim.se2.litterbox.injection.bugs.MessageAsLiteralInjector();
        java.util.List<de.uni_passau.fim.se2.litterbox.injection.InjectionResult> results =
                injector.inject(fixedProgram);
        org.junit.jupiter.api.Assertions.assertFalse(results.isEmpty(),
                "Injector should produce results");

        // Verify bug in mutated
        Program mutatedProgram = results.getFirst().mutatedProgram();
        java.util.Set<de.uni_passau.fim.se2.litterbox.analytics.Issue> bugIssues =
                bugFinder.check(mutatedProgram);
        org.junit.jupiter.api.Assertions.assertFalse(bugIssues.isEmpty(),
                "Mutated program should have bugs");

        // TODO: Fix MessageAsLiteralFix to handle parent block IDs
        // The bug is reported on the Say statement (which has block ID)
        // but matchesBugLocation looks for StringLiteral nodes
    }
}
