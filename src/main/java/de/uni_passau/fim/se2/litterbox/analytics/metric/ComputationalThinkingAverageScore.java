/*
 * Copyright (C) 2019-2021 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.analytics.metric;

import de.uni_passau.fim.se2.litterbox.analytics.MetricExtractor;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;

public class ComputationalThinkingAverageScore  implements MetricExtractor<Program> {
    @Override
    public double calculateMetric(Program program) {
        double abstractionScore = new ComputationalThinkingScoreAbstraction().calculateMetric(program);
        double dataRepresentationScore = new ComputationalThinkingScoreDataRepresentation().calculateMetric(program);
        double flowControlScore = new ComputationalThinkingScoreFlowControl().calculateMetric(program);
        double logicScore = new ComputationalThinkingScoreLogic().calculateMetric(program);
        double parallelizationScore = new ComputationalThinkingScoreParallelization().calculateMetric(program);
        double synchronizationScore = new ComputationalThinkingScoreSynchronization().calculateMetric(program);
        double userInteractivityScore = new ComputationalThinkingScoreUserInteractivity().calculateMetric(program);

        return (abstractionScore + dataRepresentationScore + flowControlScore
                + logicScore + parallelizationScore + synchronizationScore + userInteractivityScore) / 7.0;
    }

    @Override
    public String getName() {
        return "ct_score_average";
    }
}
