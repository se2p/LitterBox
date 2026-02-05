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
package de.uni_passau.fim.se2.litterbox.report;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.questions.ProgramDimension;
import de.uni_passau.fim.se2.litterbox.analytics.questions.QuestionText;
import de.uni_passau.fim.se2.litterbox.analytics.questions.QuestionType;
import de.uni_passau.fim.se2.litterbox.analytics.questions.ScopeOfFocus;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslator;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslatorFactory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static com.google.common.truth.Truth.assertThat;

public class HtmlJteQuestionResultHandlerTest implements JsonTest {

    private final IssueTranslator translator = IssueTranslatorFactory.getIssueTranslator(Locale.ENGLISH);

    @Test
    public void testTeacherModeSuppressRedundantCorrectAnswer() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/bugs/xPosEqual.json"); // Assuming this file exists from other tests

        QuestionDTO question = new QuestionDTO(
                1,
                "TestFinder",
                "Test Question",
                QuestionType.MULTIPLE_CHOICE,
                ProgramDimension.EXECUTION,
                ScopeOfFocus.BLOCK,
                "Sprite1",
                "What is correct?",
                null,
                List.of(QuestionText.fromText("A"), QuestionText.fromText("B")),
                List.of(QuestionText.fromText("A")),
                "",
                "hint"
        );

        Path tempFile = Files.createTempFile("litterbox_test_report", ".html");

        // teacherMode = true
        HtmlJteQuestionResultHandler handler = new HtmlJteQuestionResultHandler(
                translator,
                tempFile,
                true, // showAnswers
                true, // teacherMode
                false // exportMode
        );

        handler.handleResult(Path.of("TestProject.sb3"), program, Collections.singletonList(question));
        handler.finish();

        String html = Files.readString(tempFile);
        Files.deleteIfExists(tempFile);

        // Verification

        // 1. The teacher note in header should be present
        assertThat(html).contains("Teacher Mode");

        // 2. The redundant toggle button should NOT be present
        // The button has class "show-answer-btn" and text "Correct answer:"
        assertThat(html).doesNotContain("class=\"show-answer-btn\"");

        // 3. The redundant answer div should NOT be present
        // div class "correct-answer" with id starting with "answer-"
        assertThat(html).doesNotContain("class=\"correct-answer\" id=\"answer-");

        // 4. The answer choices should still be there and highlighted
        // Ideally we check for "is-correct" class on the option
        assertThat(html).contains("class=\"answer-option is-correct\"");
        assertThat(html).contains("data-value=\"a\"");
    }

    @Test
    public void testStudentModeShowsCorrectAnswerToggle() throws IOException, ParsingException {
         Program program = getAST("src/test/fixtures/bugs/xPosEqual.json");

        QuestionDTO question = new QuestionDTO(
                1,
                "TestFinder",
                "Test Question",
                QuestionType.MULTIPLE_CHOICE,
                ProgramDimension.EXECUTION,
                ScopeOfFocus.BLOCK,
                "Sprite1",
                "What is correct?",
                null,
                List.of(QuestionText.fromText("A"), QuestionText.fromText("B")),
                List.of(QuestionText.fromText("A")),
                "",
                "hint"
        );

        Path tempFile = Files.createTempFile("litterbox_test_report_student", ".html");

        // teacherMode = false, showAnswers = true
        HtmlJteQuestionResultHandler handler = new HtmlJteQuestionResultHandler(
                translator,
                tempFile,
                true, // showAnswers
                false, // teacherMode
                false // exportMode
        );

        handler.handleResult(Path.of("TestProject.sb3"), program, Collections.singletonList(question));
        handler.finish();

        String html = Files.readString(tempFile);
        Files.deleteIfExists(tempFile);

        // Verification

        // 1. The toggle button SHOULD be present
        assertThat(html).contains("class=\"show-answer-btn\"");

        // 2. The answer div SHOULD be present
        assertThat(html).contains("class=\"correct-answer\" id=\"answer-");
    }

    @Test
    public void testCustomBlocksCrashWithNPE() throws IOException, ParsingException {
        // This test reproduces the NPE when generating sprite code for projects with custom blocks
        Program program = getAST("src/test/fixtures/scratchblocks/multicustomblocks.json");

        Path tempFile = Files.createTempFile("litterbox_test_crash", ".html");

        HtmlJteQuestionResultHandler handler = new HtmlJteQuestionResultHandler(
                translator,
                tempFile,
                true
        );

        // This call triggers generateSpriteCode -> ScratchBlocksVisitor -> visit(ProcedureDefinition)
        // creating the crash if program is not set.
        handler.handleResult(Path.of("TestProject.sb3"), program, Collections.emptyList());
        handler.finish();

        String html = Files.readString(tempFile);
        Files.deleteIfExists(tempFile);

        // If we reach here, no crash occurred.
        // We can also verify that the custom block definitions were rendered in the scratchblocks code
        assertThat(html).contains("define block1");
        assertThat(html).contains("define block2");
    }

    @Test
    public void testYesNoQuestionMarking() throws IOException, ParsingException {
        // Reproduce issue: ScriptExecutionOrderSameActor produces "no" as answer for YES_NO question
        Program program = getAST("src/test/fixtures/bugs/xPosEqual.json");

        QuestionDTO question = new QuestionDTO(
                1,
                "ScriptExecutionOrderSameActor",
                "Test Question",
                QuestionType.YES_NO,
                ProgramDimension.EXECUTION,
                ScopeOfFocus.RELATION,
                "Sprite1",
                "Is this possible?",
                null,
                Collections.emptyList(),
                List.of(QuestionText.fromText("no")),
                "",
                "hint"
        );

        Path tempFile = Files.createTempFile("litterbox_test_yesno", ".html");

        HtmlJteQuestionResultHandler handler = new HtmlJteQuestionResultHandler(
                translator,
                tempFile,
                true, // showAnswers
                true, // teacherMode
                false
        );

        handler.handleResult(Path.of("TestProject.sb3"), program, Collections.singletonList(question));
        handler.finish();

        String html = Files.readString(tempFile);
        Files.deleteIfExists(tempFile);

        // This assertion fails if "no" is not recognized as the correct answer key
        assertThat(html).contains("class=\"answer-option is-correct\"");
        assertThat(html).contains("data-value=\"no\"");
    }
}
