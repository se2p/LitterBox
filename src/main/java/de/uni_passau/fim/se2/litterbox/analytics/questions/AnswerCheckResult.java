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
package de.uni_passau.fim.se2.litterbox.analytics.questions;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Complete result of checking all answers with block model breakdown.
 */
public record AnswerCheckResult(
        int totalQuestions,
        int correctAnswers,
        double score,
        Map<ProgramDimension, DimensionScore> byProgramDimension,
        Map<ScopeOfFocus, DimensionScore> byScopeOfFocus,
        List<AnswerResult> results
) {
    /**
     * Score breakdown for a dimension or scope.
     */
    public record DimensionScore(int total, int correct) {
        public double percentage() {
            return total > 0 ? (double) correct / total : 0.0;
        }
    }

    /**
     * Creates an AnswerCheckResult from a list of individual results.
     */
    public static AnswerCheckResult fromResults(
            List<AnswerResult> results,
            Map<Integer, ProgramDimension> questionDimensions,
            Map<Integer, ScopeOfFocus> questionScopes
    ) {
        int total = results.size();
        int correct = (int) results.stream().filter(AnswerResult::correct).count();
        double score = total > 0 ? (double) correct / total : 0.0;

        // Calculate by program dimension
        Map<ProgramDimension, int[]> dimCounts = new EnumMap<>(ProgramDimension.class);
        for (ProgramDimension dim : ProgramDimension.values()) {
            dimCounts.put(dim, new int[]{0, 0}); // [total, correct]
        }

        // Calculate by scope
        Map<ScopeOfFocus, int[]> scopeCounts = new EnumMap<>(ScopeOfFocus.class);
        for (ScopeOfFocus scope : ScopeOfFocus.values()) {
            scopeCounts.put(scope, new int[]{0, 0}); // [total, correct]
        }

        for (AnswerResult result : results) {
            ProgramDimension dim = questionDimensions.get(result.questionId());
            ScopeOfFocus scope = questionScopes.get(result.questionId());

            if (dim != null) {
                dimCounts.get(dim)[0]++;
                if (result.correct()) {
                    dimCounts.get(dim)[1]++;
                }
            }

            if (scope != null) {
                scopeCounts.get(scope)[0]++;
                if (result.correct()) {
                    scopeCounts.get(scope)[1]++;
                }
            }
        }

        Map<ProgramDimension, DimensionScore> byDim = new EnumMap<>(ProgramDimension.class);
        for (var entry : dimCounts.entrySet()) {
            byDim.put(entry.getKey(), new DimensionScore(entry.getValue()[0], entry.getValue()[1]));
        }

        Map<ScopeOfFocus, DimensionScore> byScope = new EnumMap<>(ScopeOfFocus.class);
        for (var entry : scopeCounts.entrySet()) {
            byScope.put(entry.getKey(), new DimensionScore(entry.getValue()[0], entry.getValue()[1]));
        }

        return new AnswerCheckResult(total, correct, score, byDim, byScope, results);
    }
}
