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

import de.uni_passau.fim.se2.litterbox.analytics.questions.*;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;
import de.uni_passau.fim.se2.litterbox.report.QuestionDTO;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslator;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;
import de.uni_passau.fim.se2.litterbox.utils.QuestionTranslator;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Analyzes a Scratch program to generate comprehension questions.
 */
public class ProgramQuestionAnalyzer implements ProgramAnalyzer<List<QuestionDTO>> {

    private final List<QuestionFinder> questionFinders;
    private final QuestionTranslator questionTranslator;
    private final Set<ProgramDimension> dimensionFilter;
    private final Set<ScopeOfFocus> scopeFilter;
    private final int maxQuestions;
    private final boolean balanced;
    private final int perCell;
    private final boolean shuffle;
    private final Long seed;

    public ProgramQuestionAnalyzer(
            List<String> finderNames,
            IssueTranslator translator,
            Set<ProgramDimension> dimensionFilter,
            Set<ScopeOfFocus> scopeFilter,
            int maxQuestions,
            boolean balanced,
            int perCell,
            boolean shuffle,
            Long seed
    ) {
        this.questionTranslator = new QuestionTranslator(translator.getLocale());
        this.dimensionFilter = dimensionFilter;
        this.scopeFilter = scopeFilter;
        this.maxQuestions = maxQuestions;
        this.balanced = balanced;
        this.perCell = perCell;
        this.shuffle = shuffle;
        this.seed = seed;

        // Get question finders
        Map<String, QuestionFinder> allFinders = IssueTool.generateQuestionFinders();
        if (finderNames == null || finderNames.isEmpty()) {
            this.questionFinders = new ArrayList<>(allFinders.values());
        } else {
            this.questionFinders = new ArrayList<>();
            for (String name : finderNames) {
                QuestionFinder finder = allFinders.get(name);
                if (finder != null) {
                    this.questionFinders.add(finder);
                }
            }
        }
    }

    @Override
    public List<QuestionDTO> analyze(Program program) {
        Preconditions.checkNotNull(program);

        // Generate all questions from all finders
        List<Question> allQuestions = new ArrayList<>();
        for (QuestionFinder finder : questionFinders) {
            Set<Question> questions = finder.check(program);

            // Apply dimension and scope filters
            for (Question question : questions) {
                if (matchesFilters(question.getFinder())) {
                    allQuestions.add(question);
                }
            }
        }

        // Convert questions to QuestionDTOs
        AtomicInteger idCounter = new AtomicInteger(1);
        List<QuestionDTO> questionDTOs = new ArrayList<>();
        for (Question question : allQuestions) {
            QuestionDTO dto = convertToDTO(question, idCounter.getAndIncrement());
            if (dto != null) {
                questionDTOs.add(dto);
            }
        }

        // Apply selection strategy
        questionDTOs = applySelectionStrategy(questionDTOs);

        return questionDTOs;
    }

    private boolean matchesFilters(QuestionFinder finder) {
        if (dimensionFilter != null && !dimensionFilter.isEmpty()) {
            if (!dimensionFilter.contains(finder.getProgramDimension())) {
                return false;
            }
        }
        if (scopeFilter != null && !scopeFilter.isEmpty()) {
            return scopeFilter.contains(finder.getScopeOfFocus());
        }
        return true;
    }

    private QuestionDTO convertToDTO(Question question, int id) {
        QuestionFinder finder = question.getFinder();

        // Get translated question text
        String questionText = question.getQuestionText();
        if (questionText == null || questionText.isEmpty()) {
            questionText = questionTranslator.getText(finder.getName());
        }

        // Get choices and answers directly from the Question object
        List<String> choices = question.getChoices().isEmpty() ? null : new ArrayList<>(question.getChoices());
        List<String> answers = question.getCorrectAnswers().isEmpty() ? null : new ArrayList<>(question.getCorrectAnswers());

        String sprite = question.getActor() != null ? question.getActor().getIdent().getName() : "Stage";

        // Get code context as scratchblocks notation with the target block highlighted
        String codeContext = ScratchBlocksVisitor.of(question);

        return new QuestionDTO(
                id,
                finder.getName(),
                questionTranslator.getName(finder.getName()),
                finder.getQuestionType(),
                finder.getProgramDimension(),
                finder.getScopeOfFocus(),
                sprite,
                questionText,
                choices,
                answers,
                codeContext,
                null
        );
    }

    private List<QuestionDTO> applySelectionStrategy(List<QuestionDTO> questions) {
        Random random = seed != null ? new Random(seed) : new Random();

        if (shuffle) {
            questions = new ArrayList<>(questions);
            Collections.shuffle(questions, random);
        }

        if (balanced || perCell > 0) {
            questions = selectBalanced(questions, random);
        }

        if (maxQuestions > 0 && questions.size() > maxQuestions) {
            questions = questions.subList(0, maxQuestions);
        }

        return questions;
    }

    private List<QuestionDTO> selectBalanced(List<QuestionDTO> questions, Random random) {
        // Group questions by block model cell
        Map<String, List<QuestionDTO>> byCell = new LinkedHashMap<>();
        for (QuestionDTO q : questions) {
            byCell.computeIfAbsent(q.getBlockModelCell(), k -> new ArrayList<>()).add(q);
        }

        // Select from each cell
        int questionsPerCell = perCell > 0 ? perCell : 1;
        List<QuestionDTO> selected = new ArrayList<>();

        for (ProgramDimension dim : ProgramDimension.values()) {
            for (ScopeOfFocus scope : ScopeOfFocus.values()) {
                String cell = dim.name() + "/" + scope.name();
                List<QuestionDTO> cellQuestions = byCell.get(cell);
                if (cellQuestions != null && !cellQuestions.isEmpty()) {
                    if (shuffle) {
                        Collections.shuffle(cellQuestions, random);
                    }
                    int count = Math.min(questionsPerCell, cellQuestions.size());
                    selected.addAll(cellQuestions.subList(0, count));
                }
            }
        }

        return selected;
    }
}
