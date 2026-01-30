package de.uni_passau.fim.se2.litterbox.report;

import de.uni_passau.fim.se2.litterbox.analytics.questions.ProgramDimension;
import de.uni_passau.fim.se2.litterbox.analytics.questions.QuestionType;
import de.uni_passau.fim.se2.litterbox.analytics.questions.ScopeOfFocus;
import de.uni_passau.fim.se2.litterbox.report.EvaluationResultHandler.EvaluatedProject;
import de.uni_passau.fim.se2.litterbox.report.EvaluationResultHandler.EvaluatedQuestion;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class EvaluationResultHandlerTest {

    @TempDir
    Path tempDir;

    @Test
    void testUnansweredQuestionsAreFiltered() throws Exception {
        // Setup
        Path answersFile = tempDir.resolve("answers.json");
        Files.writeString(answersFile, """
                {
                  "projectFile": "testProject.sb3",
                  "exportedAt": "2023-10-27T10:00:00Z",
                  "answers": [
                    {
                      "questionId": 1,
                      "answer": "My Answer"
                    }
                  ]
                }
                """);

        IssueTranslator translator = Mockito.mock(IssueTranslator.class);
        when(translator.getLocale()).thenReturn(Locale.ENGLISH);
        // Mock translator to return the key itself if not found, simulating current behavior for some keys
        when(translator.getInfo(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        when(translator.getName(anyString())).thenReturn("FinderName");

        EvaluationResultHandler handler = new EvaluationResultHandler(translator, null, answersFile, false);
        handler.begin(); // Load answers

        // Create questions
        QuestionDTO q1 = new QuestionDTO(1, "finder", "name", QuestionType.FREE_TEXT, ProgramDimension.PURPOSE, ScopeOfFocus.ATOM, null, "Q1", null, List.of("Expected"), null, null);
        QuestionDTO q2 = new QuestionDTO(2, "finder", "name", QuestionType.FREE_TEXT, ProgramDimension.PURPOSE, ScopeOfFocus.ATOM, null, "Q2", null, List.of("Expected"), null, null);

        // Execute
        handler.handleResult(Path.of("testProject.sb3"), null, List.of(q1, q2));
        handler.finish();

        // Verify
        // We need to access the evaluated projects. Since they are private, we might need reflection or
        // inspect the protected/private fields if we are in the same package.
        // Since the test class is in the same package, we can access package-private if exposed, but they are private.
        // However, we passed null as output, so it printed to console.
        // To verify properly without changing visibility, we should output to a file and read it back?
        // OR better: we can inspect the `evaluatedProjects` list via reflection for this test.

        java.lang.reflect.Field field = EvaluationResultHandler.class.getDeclaredField("evaluatedProjects");
        field.setAccessible(true);
        List<EvaluatedProject> projects = (List<EvaluatedProject>) field.get(handler);

        assertFalse(projects.isEmpty());
        EvaluatedProject project = projects.getFirst();

        // This assertion verifies the fix: Unanswered questions (not in export) are excluded
        assertEquals(1, project.questions().size(), "Should have 1 question, excluding the unasked one");
    }

    @Test
    void testFeedbackStringIsTranslated() throws Exception {
        // Setup
        Path answersFile = tempDir.resolve("answers_c.json");
        Files.writeString(answersFile, """
                {
                  "projectFile": "testProject.sb3",
                  "answers": [
                    {
                      "questionId": 1,
                      "answer": "Correct Answer"
                    }
                  ]
                }
                """);

        // Use a REAL translator or a mock that behaves like the real one to test the property file?
        // To reproduce Bug 2 (missing key in property file), we need to use the REAL IssueTranslator loading the REAL properties.
        // But IssueTranslator might be hard to instantiate with real files in a unit test depending on classpath.
        // Let's try to simulate the missing key behavior by having the mock return the key for our specific missing key.

        IssueTranslator translator = Mockito.mock(IssueTranslator.class);
        when(translator.getLocale()).thenReturn(Locale.ENGLISH);
        // Simulate successful translation
        when(translator.getInfo("evaluation_feedback_correct")).thenReturn("Correct!");
        when(translator.getName(anyString())).thenReturn("FinderName");

        EvaluationResultHandler handler = new EvaluationResultHandler(translator, null, answersFile, false);
        handler.begin();

        QuestionDTO q1 = new QuestionDTO(1, "finder", "name", QuestionType.FREE_TEXT, ProgramDimension.PURPOSE, ScopeOfFocus.ATOM, null, "Q1", null, List.of("Correct Answer"), null, null);

        handler.handleResult(Path.of("testProject.sb3"), null, List.of(q1));

        java.lang.reflect.Field field = EvaluationResultHandler.class.getDeclaredField("evaluatedProjects");
        field.setAccessible(true);
        List<EvaluatedProject> projects = (List<EvaluatedProject>) field.get(handler);

        EvaluatedQuestion eq = projects.getFirst().questions().getFirst();
        assertTrue(eq.correct());
        assertNotEquals("evaluation_feedback_correct", eq.feedback(), "Feedback should be a human readable string, not the key");
    }

    @Test
    void testNullAnswerInJsonIsHandledAsNull() throws Exception {
        // Setup
        Path answersFile = tempDir.resolve("answers_null.json");
        Files.writeString(answersFile, """
                {
                  "projectFile": "testProject.sb3",
                  "answers": [
                    {
                      "questionId": 1,
                      "answer": null
                    }
                  ]
                }
                """);

        IssueTranslator translator = Mockito.mock(IssueTranslator.class);
        when(translator.getLocale()).thenReturn(Locale.ENGLISH);
        when(translator.getInfo(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        when(translator.getName(anyString())).thenReturn("FinderName");

        EvaluationResultHandler handler = new EvaluationResultHandler(translator, null, answersFile, false);
        handler.begin();

        QuestionDTO q1 = new QuestionDTO(1, "finder", "name", QuestionType.FREE_TEXT, ProgramDimension.PURPOSE, ScopeOfFocus.ATOM, null, "Q1", null, List.of("Expected"), null, null);

        handler.handleResult(Path.of("testProject.sb3"), null, List.of(q1));

        java.lang.reflect.Field field = EvaluationResultHandler.class.getDeclaredField("evaluatedProjects");
        field.setAccessible(true);
        List<EvaluatedProject> projects = (List<EvaluatedProject>) field.get(handler);

        EvaluatedQuestion eq = projects.getFirst().questions().getFirst();

        // This is the check: should be null, not "null"
        assertNull(eq.providedAnswer(), "Provided answer should be null, not string 'null'");
    }
}
