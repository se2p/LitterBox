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

import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslator;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Interface for handling question generation results.
 */
public interface QuestionResultHandler {

    /**
     * Called before processing any projects.
     */
    void begin() throws IOException;

    /**
     * Handle the questions generated for a single project.
     *
     * @param projectFile The path to the project file
     * @param program     The parsed program
     * @param questions   The list of generated questions
     */
    void handleResult(Path projectFile, Program program, List<QuestionDTO> questions);

    /**
     * Called after all projects have been processed.
     */
    void finish() throws IOException;

    /**
     * Factory method to create an appropriate handler based on the output path.
     *
     * @param translator   The issue translator for localization
     * @param output       The output path (determines format based on extension)
     * @param showAnswers  Whether to include answers in the output
     * @return An appropriate QuestionResultHandler
     */
    static QuestionResultHandler create(
            IssueTranslator translator,
            Path output,
            boolean showAnswers
    ) {
        return create(translator, output, showAnswers, false, null);
    }

    /**
     * Factory method to create a handler with explicit format selection.
     *
     * @param translator   The issue translator for localization
     * @param output       The output path
     * @param showAnswers  Whether to include answers in the output
     * @param format       The explicit export format (null to infer from filename)
     * @return An appropriate QuestionResultHandler
     */
    static QuestionResultHandler create(
            IssueTranslator translator,
            Path output,
            boolean showAnswers,
            QuestionExportFormat format
    ) {
        return create(translator, output, showAnswers, false, format);
    }

    /**
     * Factory method to create a handler with explicit format selection and teacher mode.
     *
     * @param translator   The issue translator for localization
     * @param output       The output path
     * @param showAnswers  Whether to include answers in the output
     * @param teacherMode  Whether to generate teacher version (highlighted answers vs interactive)
     * @param format       The explicit export format (null to infer from filename)
     * @return An appropriate QuestionResultHandler
     */
    static QuestionResultHandler create(
            IssueTranslator translator,
            Path output,
            boolean showAnswers,
            boolean teacherMode,
            QuestionExportFormat format
    ) {
        return create(translator, output, showAnswers, teacherMode, false, null, false, format);
    }

    /**
     * Factory method to create a handler with all options including evaluation mode.
     *
     * @param translator   The issue translator for localization
     * @param output       The output path
     * @param showAnswers  Whether to include answers in the output
     * @param teacherMode  Whether to generate teacher version (highlighted answers vs interactive)
     * @param exportMode   Whether to generate export-only quiz (no in-browser checking)
     * @param evaluateFile Path to answers JSON file to evaluate (null for generation mode)
     * @param llmEval      Whether to use LLM for evaluating free text answers
     * @param format       The explicit export format (null to infer from filename)
     * @return An appropriate QuestionResultHandler
     */
    static QuestionResultHandler create(
            IssueTranslator translator,
            Path output,
            boolean showAnswers,
            boolean teacherMode,
            boolean exportMode,
            Path evaluateFile,
            boolean llmEval,
            QuestionExportFormat format
    ) {
        // Evaluation mode takes precedence
        if (evaluateFile != null) {
            return new EvaluationResultHandler(translator, output, evaluateFile, llmEval);
        }

        // Determine format: explicit > inferred from filename > console
        QuestionExportFormat effectiveFormat = format;
        if (effectiveFormat == null && output != null) {
            effectiveFormat = QuestionExportFormat.fromFilename(output.getFileName().toString());
        }
        if (effectiveFormat == null) {
            effectiveFormat = QuestionExportFormat.CONSOLE;
        }

        return switch (effectiveFormat) {
            case JSON -> new JSONQuestionResultHandler(output, showAnswers);
            case HTML -> new HTMLJTEQuestionResultHandler(translator, output, showAnswers, teacherMode, exportMode);
            case CONSOLE -> new ConsoleQuestionResultHandler(translator, showAnswers);
        };
    }
}
