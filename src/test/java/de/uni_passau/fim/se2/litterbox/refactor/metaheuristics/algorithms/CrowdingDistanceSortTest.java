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
package de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.algorithms;

import com.google.common.collect.Lists;
import de.uni_passau.fim.se2.litterbox.analytics.RefactoringFinder;
import de.uni_passau.fim.se2.litterbox.analytics.refactorings.MergeDoubleIfFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.RefactorSequence;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions.FitnessFunction;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions.NumberOfSmells;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators.Crossover;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators.Mutation;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators.RefactorSequenceCrossover;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators.RefactorSequenceMutation;
import de.uni_passau.fim.se2.litterbox.utils.PropertyLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

class CrowdingDistanceSortTest {

    @Test
    void crowdingDistanceIsCalculatedAndSortedCorrectly() {
        Mutation<RefactorSequence> mutation = mock(RefactorSequenceMutation.class);
        Program program = mock(Program.class);
        Crossover<RefactorSequence> crossover = mock(RefactorSequenceCrossover.class);
        List<Integer> productions = List.of(0, 0, 0);
        List<RefactoringFinder> refactoringFinders = List.of(new MergeDoubleIfFinder());

        FitnessFunction<RefactorSequence> function1 = mock(NumberOfSmells.class);
        FitnessFunction<RefactorSequence> function2 = mock(NumberOfSmells.class);

        Map<FitnessFunction<RefactorSequence>, Double> fitnessMap1 = new HashMap<>();
        fitnessMap1.put(function1, 0.99);
        fitnessMap1.put(function2, 0.90);
        RefactorSequence c1 = new RefactorSequence(program, mutation, crossover, productions, refactoringFinders);

        Map<FitnessFunction<RefactorSequence>, Double> fitnessMap3 = new HashMap<>();
        fitnessMap3.put(function1, 0.95);
        fitnessMap3.put(function2, 0.92);
        RefactorSequence c3 = new RefactorSequence(program, mutation, crossover, productions, refactoringFinders);

        List<RefactorSequence> solutions = Lists.newArrayList(c1, c1, c3);

        CrowdingDistanceSort<RefactorSequence> crowdingDistanceSort = new CrowdingDistanceSort<>(List.of(function1, function2));
        crowdingDistanceSort.calculateCrowdingDistanceAndSort(solutions);

        assertSame(c1, solutions.get(0));
        assertSame(c1, solutions.get(1));
        assertSame(c3, solutions.get(2));
    }
}
