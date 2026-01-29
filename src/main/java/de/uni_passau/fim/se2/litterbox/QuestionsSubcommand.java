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
package de.uni_passau.fim.se2.litterbox;

import de.uni_passau.fim.se2.litterbox.analytics.QuestionAnalyzer;
import de.uni_passau.fim.se2.litterbox.analytics.questions.ProgramDimension;
import de.uni_passau.fim.se2.litterbox.analytics.questions.ScopeOfFocus;
import de.uni_passau.fim.se2.litterbox.report.QuizExportFormat;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslator;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslatorFactory;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * CLI subcommand for generating comprehension questions about Scratch projects.
 */
@CommandLine.Command(
        name = "questions",
        description = "Generate comprehension questions for Scratch projects.",
        mixinStandardHelpOptions = true
)
public class QuestionsSubcommand extends Main.LitterBoxSubcommand {

    @CommandLine.Option(
            names = {"-l", "--lang"},
            description = "Language for question text (e.g., 'en', 'de').",
            defaultValue = "en"
    )
    String language;

    @CommandLine.Option(
            names = {"-q", "--question-finders"},
            description = "Specific question finders to use, separated by commas.",
            split = ","
    )
    List<String> questionFinders;

    @CommandLine.Option(
            names = {"-n", "--max-questions"},
            description = "Maximum number of questions per project (-1 for all).",
            defaultValue = "-1"
    )
    int maxQuestions;

    @CommandLine.Option(
            names = {"--program-dimension"},
            description = "Filter by program dimension: TEXT, EXECUTION, PURPOSE.",
            split = ","
    )
    Set<ProgramDimension> programDimensions;

    @CommandLine.Option(
            names = {"--scope"},
            description = "Filter by scope of focus: ATOM, BLOCK, RELATION, MACRO.",
            split = ","
    )
    Set<ScopeOfFocus> scopes;

    @CommandLine.Option(
            names = {"--balanced"},
            description = "Select at least 1 question per block model cell (if available)."
    )
    boolean balanced;

    @CommandLine.Option(
            names = {"--per-cell"},
            description = "Maximum questions per block model cell (implies --balanced).",
            defaultValue = "0"
    )
    int perCell;

    @CommandLine.Option(
            names = {"--shuffle"},
            description = "Randomize question order."
    )
    boolean shuffle;

    @CommandLine.Option(
            names = {"--seed"},
            description = "Random seed for reproducibility."
    )
    Long seed;



    @CommandLine.Option(
            names = {"--teacher-mode"},
            description = "Generate teacher version with highlighted correct answers instead of interactive checking (HTML output only)."
    )
    boolean teacherMode;

    @CommandLine.Option(
            names = {"--export-format", "-f"},
            description = "Export format: CONSOLE, JSON, HTML. " +
                    "If not specified, format is inferred from output file extension."
    )
    QuizExportFormat exportFormat;

    @CommandLine.Option(
            names = {"--export-mode"},
            description = "Generate HTML quiz for answer export (HTML output only)."
    )
    boolean exportMode;

    @CommandLine.Option(
            names = {"--evaluate"},
            description = "Evaluate answers from a JSON file exported from an HTML quiz."
    )
    java.nio.file.Path evaluateFile;

    @CommandLine.Option(
            names = {"--llm-eval"},
            description = "Use LLM to evaluate free text answers (requires --evaluate)."
    )
    boolean llmEval;

    private IssueTranslator translator;

    @Override
    protected QuestionAnalyzer getAnalyzer() throws IOException {
        translator = IssueTranslatorFactory.getIssueTranslator(Locale.of(language));

        if (projectInput.projectPath == null) {
            projectInput.projectPath = Files.createTempDirectory("litterbox-questions");
        }

        // If perCell is set, enable balanced mode
        boolean effectiveBalanced = balanced || perCell > 0;

        // Determine effective options based on format
        QuizExportFormat effectiveFormat = exportFormat;
        if (effectiveFormat == null && commonOptions.outputPath != null) {
            effectiveFormat = QuizExportFormat.fromFilename(commonOptions.outputPath.getFileName().toString());
        }
        if (effectiveFormat == null) {
            effectiveFormat = QuizExportFormat.CONSOLE;
        }

        // JSON export should always include answers.
        // For HTML, teacherMode determines if answers are shown/highlighted.
        // For Console, we effectively disable explicit answer showing since teacherMode is restricted to HTML.
        boolean effectiveShowAnswers = effectiveFormat == QuizExportFormat.JSON;
        boolean effectiveTeacherMode = teacherMode;
        boolean effectiveExportMode = exportMode;

        if (effectiveFormat != QuizExportFormat.HTML) {
            // Restrict teacherMode and exportMode to HTML
            if (teacherMode || exportMode) {
                // User requested restricted options for non-HTML format.
                // We ignore them as requested ("should not change anything") but log/warn if possible,
                // or just silently enforce the restriction.
                // Here we just ensure they don't affect the output logic.
                effectiveTeacherMode = false;
                effectiveExportMode = false;
            }
        } else {
            // HTML format: teacherMode implies showing answers
            effectiveShowAnswers = teacherMode;
        }

        return new QuestionAnalyzer.Builder(translator, commonOptions.outputPath)
                .withDeleteProject(commonOptions.deleteProject)
                .withQuestionFinders(questionFinders)
                .withDimensionFilter(programDimensions)
                .withScopeFilter(scopes)
                .withMaxQuestions(maxQuestions)
                .withBalanced(effectiveBalanced)
                .withPerCell(perCell)
                .withShuffle(shuffle)
                .withSeed(seed)
                .withShowAnswers(effectiveShowAnswers)
                .withTeacherMode(effectiveTeacherMode)
                .withExportMode(effectiveExportMode)
                .withEvaluation(evaluateFile, llmEval)
                .withExportFormat(effectiveFormat)
                .build();
    }
}
