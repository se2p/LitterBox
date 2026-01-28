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

import de.uni_passau.fim.se2.litterbox.analytics.questions.ProgramDimension;
import de.uni_passau.fim.se2.litterbox.analytics.questions.ScopeOfFocus;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslator;

import java.nio.file.Path;
import java.util.*;

/**
 * Outputs questions to the console with block model tags.
 */
public class ConsoleQuestionResultHandler implements QuestionResultHandler {

    private final IssueTranslator translator;
    private final boolean showAnswers;

    public ConsoleQuestionResultHandler(IssueTranslator translator, boolean showAnswers) {
        this.translator = translator;
        this.showAnswers = showAnswers;
    }

    @Override
    public void begin() {
        // Nothing to do
    }

    @Override
    public void handleResult(Path projectFile, Program program, List<QuestionDTO> questions) {
        System.out.println();
        System.out.println("Questions for project: " + projectFile.getFileName());
        System.out.println();

        // Calculate coverage
        Map<String, Integer> coverage = calculateCoverage(questions);
        printCoverageSummary(coverage);
        System.out.println();

        for (QuestionDTO question : questions) {
            printQuestion(question);
        }

        // Print dimension summary
        printDimensionSummary(questions);
    }

    private void printQuestion(QuestionDTO question) {
        System.out.printf("[%s/%s] %s (%s)%n",
                question.programDimension(),
                question.scopeOfFocus(),
                translator.getName(question.finder()),
                question.questionType().name().toLowerCase());
        System.out.println("  Sprite: " + question.sprite());
        System.out.println("  Q: " + question.questionText());

        if (question.choices() != null && !question.choices().isEmpty()) {
            System.out.println("  Choices: " + String.join(", ", question.choices()));
        }

        if (showAnswers && question.correctAnswers() != null && !question.correctAnswers().isEmpty()) {
            System.out.println("  Answer: " + String.join(", ", question.correctAnswers()));
        }

        System.out.println();
    }

    private Map<String, Integer> calculateCoverage(List<QuestionDTO> questions) {
        Map<String, Integer> coverage = new LinkedHashMap<>();
        for (QuestionDTO q : questions) {
            String cell = q.getBlockModelCell();
            coverage.merge(cell, 1, Integer::sum);
        }
        return coverage;
    }

    private void printCoverageSummary(Map<String, Integer> coverage) {
        Set<String> allCells = new LinkedHashSet<>();
        for (ProgramDimension dim : ProgramDimension.values()) {
            for (ScopeOfFocus scope : ScopeOfFocus.values()) {
                allCells.add(dim.name() + "/" + scope.name());
            }
        }

        Set<String> missingCells = new LinkedHashSet<>();
        for (String cell : allCells) {
            if (!coverage.containsKey(cell)) {
                missingCells.add(cell);
            }
        }

        int coveredCount = allCells.size() - missingCells.size();
        System.out.print("[Coverage: " + coveredCount + "/" + allCells.size() + " cells");
        if (!missingCells.isEmpty()) {
            System.out.print(", missing: " + String.join(", ", missingCells));
        }
        System.out.println("]");
    }

    private void printDimensionSummary(List<QuestionDTO> questions) {
        Map<ProgramDimension, Integer> byDimension = new EnumMap<>(ProgramDimension.class);
        for (QuestionDTO q : questions) {
            byDimension.merge(q.programDimension(), 1, Integer::sum);
        }

        StringBuilder summary = new StringBuilder("Summary: " + questions.size() + " questions (");
        List<String> parts = new ArrayList<>();
        for (ProgramDimension dim : ProgramDimension.values()) {
            int count = byDimension.getOrDefault(dim, 0);
            parts.add(dim.name() + ": " + count);
        }
        summary.append(String.join(", ", parts));
        summary.append(")");
        System.out.println(summary);
    }

    @Override
    public void finish() {
        // Nothing to do
    }
}
