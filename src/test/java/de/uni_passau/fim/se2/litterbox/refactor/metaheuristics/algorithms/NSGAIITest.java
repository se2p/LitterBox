/*
 * Copyright (C) 2019-2025 LitterBox contributors
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
import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.RefactoringFinder;
import de.uni_passau.fim.se2.litterbox.analytics.refactorings.BlockFinder;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.ControlStmt;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.*;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions.FitnessFunction;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions.NumberOfControlBlocks;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions.NumberOfHelloBlocks;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators.*;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.DeleteControlBlock;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.Refactoring;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class NSGAIITest implements JsonTest {

    @BeforeEach
    void setupEnv() {
        System.setProperty("generations", "2");
    }

    @Test
    @SuppressWarnings("unchecked")
    void testNSGAIICalls() {
        RefactorSequence c1gen1 = mock(RefactorSequence.class);
        RefactorSequence c2gen1 = mock(RefactorSequence.class);
        RefactorSequence c3gen1 = mock(RefactorSequence.class);
        RefactorSequence c4gen1 = mock(RefactorSequence.class);
        List<RefactorSequence> gen1 = Lists.newArrayList(c1gen1, c2gen1, c3gen1, c4gen1);

        RefactorSequence c1gen2 = mock(RefactorSequence.class);
        RefactorSequence c2gen2 = mock(RefactorSequence.class);
        RefactorSequence c3gen2 = mock(RefactorSequence.class);
        RefactorSequence c4gen2 = mock(RefactorSequence.class);
        List<RefactorSequence> gen2 = Lists.newArrayList(c1gen2, c2gen2, c3gen2, c4gen2);

        List<RefactorSequence> combined = Lists.newLinkedList(gen1);
        combined.addAll(gen2);

        List<RefactorSequence> front1 = List.of(c1gen1, c1gen2);
        List<RefactorSequence> front2 = List.of(c2gen1, c2gen2);
        List<RefactorSequence> front3 = List.of(c3gen1, c3gen2);
        List<RefactorSequence> front4 = List.of(c4gen1, c4gen2);

        List<List<RefactorSequence>> paretoFronts = Lists.newArrayList(front1, front2, front3, front4);

        FixedSizePopulationGenerator<RefactorSequence> populationGenerator = mock(FixedSizePopulationGenerator.class);
        when(populationGenerator.getPopulationSize()).thenReturn(4);
        when((populationGenerator.get())).thenReturn(gen1);

        OffspringGenerator<RefactorSequence> offspringGenerator = mock(OffspringGenerator.class);
        when(offspringGenerator.generateOffspring(gen1)).thenReturn(gen2);

        FastNonDominatedSort<RefactorSequence> fastNonDominatedSort = mock(FastNonDominatedSort.class);
        when(fastNonDominatedSort.fastNonDominatedSort(combined)).thenReturn(paretoFronts);

        CrowdingDistanceSort<RefactorSequence> crowdingDistanceSort = mock(CrowdingDistanceSort.class);
        doNothing().when(crowdingDistanceSort).calculateCrowdingDistanceAndSort(anyList());

        List<RefactorSequence> populationAfterNSGAII = Lists.newArrayList(c1gen1, c1gen2, c2gen1, c2gen2);
        ignoreStubs(offspringGenerator);

        List<List<RefactorSequence>> finalFronts = List.of(front1, front2);
        when(fastNonDominatedSort.fastNonDominatedSort(populationAfterNSGAII)).thenReturn(finalFronts);

        GeneticAlgorithm<RefactorSequence> nsgaii = new NSGAII<>(populationGenerator, offspringGenerator, fastNonDominatedSort, crowdingDistanceSort);
        List<RefactorSequence> nsgaiiSolution = nsgaii.findSolution();
        assertEquals(2, nsgaiiSolution.size());
        assertEquals(front1, nsgaiiSolution);
    }

    @Test
    @SuppressWarnings("unchecked")
    void evolveOnEmptyPopulation() {
        List<RefactorSequence> emptyList = List.of();

        FixedSizePopulationGenerator<RefactorSequence> populationGenerator = mock(FixedSizePopulationGenerator.class);
        OffspringGenerator<RefactorSequence> offspringGenerator = mock(OffspringGenerator.class);
        when(offspringGenerator.generateOffspring(emptyList)).thenReturn(emptyList);

        FastNonDominatedSort<RefactorSequence> fastNonDominatedSort = mock(FastNonDominatedSort.class);
        CrowdingDistanceSort<RefactorSequence> crowdingDistanceSort = mock(CrowdingDistanceSort.class);

        when(fastNonDominatedSort.fastNonDominatedSort(emptyList)).thenReturn(List.of());
        NSGAII<RefactorSequence> nsgaii = new NSGAII<>(populationGenerator, offspringGenerator, fastNonDominatedSort, crowdingDistanceSort);
        assertTrue(nsgaii.evolve(emptyList).isEmpty());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testPopulationTruncation() {
        RefactorSequence c1 = mock(RefactorSequence.class);
        RefactorSequence c2 = mock(RefactorSequence.class);
        RefactorSequence c3 = mock(RefactorSequence.class);
        RefactorSequence c4 = mock(RefactorSequence.class);
        List<RefactorSequence> population = Lists.newArrayList(c1, c2, c3, c4);

        // 1 front with 4 solutions
        List<List<RefactorSequence>> fronts = List.of(population);

        FixedSizePopulationGenerator<RefactorSequence> populationGenerator = mock(FixedSizePopulationGenerator.class);
        when(populationGenerator.getPopulationSize()).thenReturn(2); // Truncate to 2

        OffspringGenerator<RefactorSequence> offspringGenerator = mock(OffspringGenerator.class);
        when(offspringGenerator.generateOffspring(anyList())).thenReturn(List.of());

        FastNonDominatedSort<RefactorSequence> fastNonDominatedSort = mock(FastNonDominatedSort.class);
        when(fastNonDominatedSort.fastNonDominatedSort(population)).thenReturn(fronts);

        CrowdingDistanceSort<RefactorSequence> crowdingDistanceSort = mock(CrowdingDistanceSort.class);
        // Mock sorting to ensure c1 and c2 are first (if implementation relies on list order after sort)
        doAnswer(invocation -> {
            List<RefactorSequence> list = invocation.getArgument(0);
            // Keep order c1, c2, c3, c4
            return null;
        }).when(crowdingDistanceSort).calculateCrowdingDistanceAndSort(anyList());

        NSGAII<RefactorSequence> nsgaii = new NSGAII<>(populationGenerator, offspringGenerator, fastNonDominatedSort, crowdingDistanceSort);
        List<RefactorSequence> result = nsgaii.evolve(population);

        assertEquals(2, result.size());
        assertTrue(result.contains(c1));
        assertTrue(result.contains(c2));
        verify(crowdingDistanceSort).calculateCrowdingDistanceAndSort(anyList());
    }

    @Test
    void testTimeLimitTermination() throws Exception {
        // Set MAX_GEN to 100, MAX_SECONDS to 1 using constructor
        int maxGen = 100;
        int maxSeconds = 1;

        FixedSizePopulationGenerator<RefactorSequence> populationGenerator = mock(FixedSizePopulationGenerator.class);
        when(populationGenerator.get()).thenReturn(new ArrayList<>());
        when(populationGenerator.getPopulationSize()).thenReturn(10);
        
        OffspringGenerator<RefactorSequence> offspringGenerator = mock(OffspringGenerator.class);
        when(offspringGenerator.generateOffspring(anyList())).thenReturn(new ArrayList<>());

        FastNonDominatedSort<RefactorSequence> fastNonDominatedSort = mock(FastNonDominatedSort.class);
        when(fastNonDominatedSort.fastNonDominatedSort(anyList())).thenReturn(List.of(new ArrayList<>()));
        when(fastNonDominatedSort.getFitnessFunctions()).thenReturn(List.of());

        CrowdingDistanceSort<RefactorSequence> crowdingDistanceSort = mock(CrowdingDistanceSort.class);

        NSGAII<RefactorSequence> nsgaii = spy(new NSGAII<>(populationGenerator, offspringGenerator, fastNonDominatedSort, crowdingDistanceSort, maxGen, maxSeconds));
        
        // Slow down evolve to ensure time limit is hit before 100 generations
        doAnswer(invocation -> {
            Thread.sleep(600); // 0.6s per iteration. 2 iterations > 1s.
            return new ArrayList<>();
        }).when(nsgaii).evolve(anyList());

        nsgaii.findSolution();

        // Should run at most 2 iterations (0.6 + 0.6 = 1.2 > 1.0)
        assertTrue(nsgaii.getIteration() < 100, "Should terminate before 100 generations due to time limit");
        assertTrue(nsgaii.getIteration() >= 1, "Should run at least 1 iteration");
    }
}
