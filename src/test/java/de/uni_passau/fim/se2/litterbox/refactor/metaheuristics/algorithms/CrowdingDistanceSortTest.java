package de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.algorithms;

import com.google.common.collect.Lists;
import de.uni_passau.fim.se2.litterbox.analytics.RefactoringFinder;
import de.uni_passau.fim.se2.litterbox.analytics.refactorings.DoubleIfFinder;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.RefactorSequence;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators.Crossover;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators.Mutation;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators.RefactorSequenceCrossover;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators.RefactorSequenceMutation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

class CrowdingDistanceSortTest {

    Mutation<RefactorSequence> mutation = mock(RefactorSequenceMutation.class);
    Crossover<RefactorSequence> crossover = mock(RefactorSequenceCrossover.class);
    List<Integer> productions = List.of(0, 0, 0);
    List<RefactoringFinder> refactoringFinders = List.of(new DoubleIfFinder());

    @Test
    void crowdingDistanceIsCalculatedAndSortedCorrectly() {

        RefactorSequence x6 = new RefactorSequence(mutation, crossover, productions, refactoringFinders);
        x6.setFitness1(0.99);

        RefactorSequence x7 = new RefactorSequence(mutation, crossover, productions, refactoringFinders);
        x7.setFitness1(0.99);

        RefactorSequence x8 = new RefactorSequence(mutation, crossover, productions, refactoringFinders);
        x8.setFitness1(0.95);

        List<RefactorSequence> solutions = Lists.newArrayList(x6, x7, x8);

        CrowdingDistanceSort<RefactorSequence> crowdingDistanceSort = new CrowdingDistanceSort<>();
        crowdingDistanceSort.calculateCrowdingDistanceAndSort(solutions);

        assertSame(x7, solutions.get(0));
        assertSame(x8, solutions.get(1));
        assertSame(x6, solutions.get(2));
    }
}
