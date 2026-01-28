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
package de.uni_passau.fim.se2.litterbox.analytics;

import de.uni_passau.fim.se2.litterbox.analytics.questions.ProgramDimension;
import de.uni_passau.fim.se2.litterbox.analytics.questions.ScopeOfFocus;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.report.QuestionDTO;
import de.uni_passau.fim.se2.litterbox.report.QuestionResultHandler;
import de.uni_passau.fim.se2.litterbox.report.QuizExportFormat;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslator;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * File analyzer for generating comprehension questions from Scratch projects.
 */
public class QuestionAnalyzer extends FileAnalyzer<List<QuestionDTO>> {

    private static final Logger log = Logger.getLogger(QuestionAnalyzer.class.getName());

    private final QuestionResultHandler resultHandler;

    public QuestionAnalyzer(
            IssueTranslator translator,
            Path output,
            boolean delete,
            List<String> questionFinders,
            Set<ProgramDimension> dimensionFilter,
            Set<ScopeOfFocus> scopeFilter,
            int maxQuestions,
            boolean balanced,
            int perCell,
            boolean shuffle,
            Long seed,
            boolean showAnswers
    ) {
        this(translator, output, delete, questionFinders, dimensionFilter, scopeFilter,
                maxQuestions, balanced, perCell, shuffle, seed, showAnswers, false, null);
    }

    public QuestionAnalyzer(
            IssueTranslator translator,
            Path output,
            boolean delete,
            List<String> questionFinders,
            Set<ProgramDimension> dimensionFilter,
            Set<ScopeOfFocus> scopeFilter,
            int maxQuestions,
            boolean balanced,
            int perCell,
            boolean shuffle,
            Long seed,
            boolean showAnswers,
            QuizExportFormat exportFormat
    ) {
        this(translator, output, delete, questionFinders, dimensionFilter, scopeFilter,
                maxQuestions, balanced, perCell, shuffle, seed, showAnswers, false, exportFormat);
    }

    public QuestionAnalyzer(
            IssueTranslator translator,
            Path output,
            boolean delete,
            List<String> questionFinders,
            Set<ProgramDimension> dimensionFilter,
            Set<ScopeOfFocus> scopeFilter,
            int maxQuestions,
            boolean balanced,
            int perCell,
            boolean shuffle,
            Long seed,
            boolean showAnswers,
            boolean teacherMode,
            QuizExportFormat exportFormat
    ) {
        super(
                new ProgramQuestionAnalyzer(
                        questionFinders,
                        translator,
                        dimensionFilter,
                        scopeFilter,
                        maxQuestions,
                        balanced,
                        perCell,
                        shuffle,
                        seed
                ),
                output,
                delete
        );
        this.resultHandler = QuestionResultHandler.create(translator, output, showAnswers, teacherMode, exportFormat);
    }

    @Override
    protected void beginAnalysis() throws IOException {
        resultHandler.begin();
        super.beginAnalysis();
    }

    @Override
    protected void endAnalysis() throws IOException {
        resultHandler.finish();
        super.endAnalysis();
    }

    @Override
    protected void writeResultToFile(Path projectFile, Program program, List<QuestionDTO> result) {
        resultHandler.handleResult(projectFile, program, result);
    }
}
