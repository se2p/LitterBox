package de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.algorithms;

import com.google.common.collect.Lists;
import de.uni_passau.fim.se2.litterbox.analytics.RefactoringFinder;
import de.uni_passau.fim.se2.litterbox.analytics.refactorings.DoubleIfFinder;
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

    @BeforeEach
    void setupEnv() {
        PropertyLoader.setDefaultSystemProperties("nsga-ii.properties");
    }

    @Test
    void crowdingDistanceIsCalculatedAndSortedCorrectly() {
        Mutation<RefactorSequence> mutation = mock(RefactorSequenceMutation.class);
        Crossover<RefactorSequence> crossover = mock(RefactorSequenceCrossover.class);
        List<Integer> productions = List.of(0, 0, 0);
        List<RefactoringFinder> refactoringFinders = List.of(new DoubleIfFinder());

        FitnessFunction<RefactorSequence> function1 = mock(NumberOfSmells.class);
        FitnessFunction<RefactorSequence> function2 = mock(NumberOfSmells.class);

        Map<FitnessFunction<RefactorSequence>, Double> fitnessMap1 = new HashMap<>();
        fitnessMap1.put(function1, 0.99);
        fitnessMap1.put(function2, 0.90);
        RefactorSequence c1 = new RefactorSequence(mutation, crossover, productions, refactoringFinders);

        Map<FitnessFunction<RefactorSequence>, Double> fitnessMap3 = new HashMap<>();
        fitnessMap3.put(function1, 0.95);
        fitnessMap3.put(function2, 0.92);
        RefactorSequence c3 = new RefactorSequence(mutation, crossover, productions, refactoringFinders);

        List<RefactorSequence> solutions = Lists.newArrayList(c1, c1, c3);

        CrowdingDistanceSort<RefactorSequence> crowdingDistanceSort = new CrowdingDistanceSort<>(List.of(function1, function2));
        crowdingDistanceSort.calculateCrowdingDistanceAndSort(solutions);

        assertSame(c1, solutions.get(0));
        assertSame(c1, solutions.get(1));
        assertSame(c3, solutions.get(2));
    }
}
