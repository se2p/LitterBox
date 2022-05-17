/*
 * Copyright (C) 2019-2022 LitterBox contributors
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

import de.uni_passau.fim.se2.litterbox.analytics.metric.SliceCoverage;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.RefactorSequence;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.averagingDouble;

public class AverageCohesionFitness implements MaximizingFitnessFunction<RefactorSequence> {
    private static final String NAME = "average_cohesion_fitness";

    @Override
    public double getFitness(RefactorSequence refactorSequence) throws NullPointerException {
        Program refactoredProgram = refactorSequence.getRefactoredProgram();

        List<Double> coverage = new ArrayList<>();

        refactoredProgram.accept(new ScratchVisitor() {
            @Override
            public void visit(Script node) {
                SliceCoverage<Script> sliceCoverage = new SliceCoverage<>();
                coverage.add(sliceCoverage.calculateMetric(node));
            }
        });

        return coverage.stream().collect(averagingDouble(Number::doubleValue));
    }

    @Override
    public String getName() {
        return NAME;
    }
}
