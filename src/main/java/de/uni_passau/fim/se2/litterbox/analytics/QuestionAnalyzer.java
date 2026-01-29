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
import de.uni_passau.fim.se2.litterbox.report.QuestionExportFormat;
import de.uni_passau.fim.se2.litterbox.report.QuestionResultHandler;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslator;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * File analyzer for generating comprehension questions from Scratch projects.
 */
public class QuestionAnalyzer extends FileAnalyzer<List<QuestionDTO>> {

    private final QuestionResultHandler resultHandler;

    private QuestionAnalyzer(Builder builder) {
        super(
                new ProgramQuestionAnalyzer(
                        builder.questionFinders,
                        builder.translator,
                        builder.dimensionFilter,
                        builder.scopeFilter,
                        builder.maxQuestions,
                        builder.balanced,
                        builder.perCell,
                        builder.shuffle,
                        builder.seed
                ),
                builder.output,
                builder.delete
        );
        this.resultHandler = QuestionResultHandler.create(
                builder.translator,
                builder.output,
                builder.showAnswers,
                builder.teacherMode,
                builder.exportMode,
                builder.evaluateFile,
                builder.llmEval,
                builder.exportFormat
        );
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

    /**
     * Builder for {@link QuestionAnalyzer}.
     */
    public static class Builder {
        private final IssueTranslator translator;
        private final Path output;

        // Optional parameters with defaults
        private boolean delete = false;
        private List<String> questionFinders = Collections.emptyList();
        private Set<ProgramDimension> dimensionFilter = Collections.emptySet();
        private Set<ScopeOfFocus> scopeFilter = Collections.emptySet();
        private int maxQuestions = -1;
        private boolean balanced = false;
        private int perCell = 0;
        private boolean shuffle = false;
        private Long seed = null;
        private boolean showAnswers = false;
        private boolean teacherMode = false;
        private boolean exportMode = false;
        private Path evaluateFile = null;
        private boolean llmEval = false;
        private QuestionExportFormat exportFormat = null;

        /**
         * Creates a new builder with required parameters.
         *
         * @param translator The issue translator
         * @param output     The output path
         */
        public Builder(IssueTranslator translator, Path output) {
            Preconditions.checkNotNull(translator);
            this.translator = translator;
            this.output = output;
        }

        public Builder withDeleteProject(boolean delete) {
            this.delete = delete;
            return this;
        }

        public Builder withQuestionFinders(List<String> questionFinders) {
            this.questionFinders = questionFinders != null ? questionFinders : Collections.emptyList();
            return this;
        }

        public Builder withDimensionFilter(Set<ProgramDimension> dimensionFilter) {
            this.dimensionFilter = dimensionFilter != null ? dimensionFilter : Collections.emptySet();
            return this;
        }

        public Builder withScopeFilter(Set<ScopeOfFocus> scopeFilter) {
            this.scopeFilter = scopeFilter != null ? scopeFilter : Collections.emptySet();
            return this;
        }

        public Builder withMaxQuestions(int maxQuestions) {
            this.maxQuestions = maxQuestions;
            return this;
        }

        public Builder withBalanced(boolean balanced) {
            this.balanced = balanced;
            return this;
        }

        public Builder withPerCell(int perCell) {
            this.perCell = perCell;
            return this;
        }

        public Builder withShuffle(boolean shuffle) {
            this.shuffle = shuffle;
            return this;
        }

        public Builder withSeed(Long seed) {
            this.seed = seed;
            return this;
        }

        public Builder withShowAnswers(boolean showAnswers) {
            this.showAnswers = showAnswers;
            return this;
        }

        public Builder withTeacherMode(boolean teacherMode) {
            this.teacherMode = teacherMode;
            return this;
        }

        public Builder withExportMode(boolean exportMode) {
            this.exportMode = exportMode;
            return this;
        }

        public Builder withEvaluation(Path evaluateFile, boolean llmEval) {
            this.evaluateFile = evaluateFile;
            this.llmEval = llmEval;
            return this;
        }

        public Builder withExportFormat(QuestionExportFormat exportFormat) {
            this.exportFormat = exportFormat;
            return this;
        }

        public QuestionAnalyzer build() {
            return new QuestionAnalyzer(this);
        }
    }
}
