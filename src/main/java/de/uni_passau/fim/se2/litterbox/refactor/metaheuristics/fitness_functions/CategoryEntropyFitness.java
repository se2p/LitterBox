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
package de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions;

import de.uni_passau.fim.se2.litterbox.analytics.metric.CategoryEntropy;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.RefactorSequence;

public class CategoryEntropyFitness implements MinimizingFitnessFunction<RefactorSequence> {

    private static final String NAME = "category_entropy_fitness";

    @Override
    public double getFitness(RefactorSequence refactorSequence) {
        Program refactoredProgram = refactorSequence.getRefactoredProgram();

        CategoryEntropy<Program> entropy = new CategoryEntropy<>();
        return entropy.calculateMetric(refactoredProgram);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
