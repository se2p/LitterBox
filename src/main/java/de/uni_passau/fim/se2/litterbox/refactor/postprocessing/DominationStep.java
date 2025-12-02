/*
 * Copyright (C) 2019-2024 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.refactor.postprocessing;

import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.algorithms.Dominance;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.RefactorSequence;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions.FitnessFunction;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public class DominationStep implements PostProcessorStep {
    private static final Logger log = Logger.getLogger(DominationStep.class.getName());

    @Override
    public List<RefactorSequence> apply(List<RefactorSequence> solutions, PostProcessorContext context) {
        if (solutions.isEmpty()) {
            return solutions;
        }

        Dominance<RefactorSequence> dominance = new Dominance<>(context.getFitnessFunctions());

        // Create an empty sequence representing the original program
        RefactorSequence first = solutions.getFirst();
        RefactorSequence original = new RefactorSequence(
                context.getOriginalProgram(),
                first.getMutation(),
                first.getCrossover(),
                Collections.emptyList()
        );
        List<RefactorSequence> dominatingSolutions = solutions.stream()
                .filter(solution -> dominance.test(solution, original))
                .toList();

        log.info("Dominating solutions: " + dominatingSolutions.size());
        if (dominatingSolutions.isEmpty()) {
            log.info("No solutions found that dominate the original program. Checking for non-dominated solutions.");
            List<RefactorSequence> nonDominatedSolutions = solutions.stream()
                    .filter(solution -> !dominance.test(original, solution))
                    .filter(solution -> isBetterInAtLeastOne(solution, original, context.getFitnessFunctions()))
                    .toList();
            log.info("Non-dominated solutions: " + nonDominatedSolutions.size());
            return nonDominatedSolutions;
        }
        return dominatingSolutions;
    }

    private boolean isBetterInAtLeastOne(RefactorSequence solution, RefactorSequence original, List<de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions.FitnessFunction<RefactorSequence>> fitnessFunctions) {
        for (FitnessFunction<RefactorSequence> ff : fitnessFunctions) {            
            double solFit = solution.getFitness(ff);
            double orgFit = original.getFitness(ff);
            
            if (ff.isMinimizing()) {
                if (solFit < orgFit) {
                    return true;
                }
            } else {
                if (solFit > orgFit) {
                    return true;
                }
            }
        }
        return false;
    }
}
