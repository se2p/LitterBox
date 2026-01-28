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
            names = {"--show-answers"},
            description = "Include correct answers in output."
    )
    boolean showAnswers;

    @CommandLine.Option(
            names = {"--teacher-mode"},
            description = "Generate teacher version with highlighted correct answers instead of interactive checking."
    )
    boolean teacherMode;

    @CommandLine.Option(
            names = {"--export-format", "-f"},
            description = "Export format: CONSOLE, JSON, HTML. " +
                    "If not specified, format is inferred from output file extension."
    )
    QuizExportFormat exportFormat;

    private IssueTranslator translator;

    @Override
    protected QuestionAnalyzer getAnalyzer() throws IOException {
        translator = IssueTranslatorFactory.getIssueTranslator(Locale.of(language));

        if (projectInput.projectPath == null) {
            projectInput.projectPath = Files.createTempDirectory("litterbox-questions");
        }

        // If perCell is set, enable balanced mode
        boolean effectiveBalanced = balanced || perCell > 0;

        return new QuestionAnalyzer(
                translator,
                commonOptions.outputPath,
                commonOptions.deleteProject,
                questionFinders,
                programDimensions,
                scopes,
                maxQuestions,
                effectiveBalanced,
                perCell,
                shuffle,
                seed,
                showAnswers,
                teacherMode,
                exportFormat
        );
    }
}
