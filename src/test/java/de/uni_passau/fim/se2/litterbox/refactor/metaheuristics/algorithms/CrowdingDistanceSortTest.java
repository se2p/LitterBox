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
package de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.algorithms;

import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.Solution;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions.FitnessFunction;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators.Crossover;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators.Mutation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CrowdingDistanceSortTest {

    static class TestSolution extends Solution<TestSolution> {
        protected TestSolution() {
            super(mock(Mutation.class), mock(Crossover.class));
        }

        @Override
        public TestSolution self() {
            return this;
        }

        @Override
        public TestSolution copy() {
            return new TestSolution();
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(this);
        }

        @Override
        public boolean equals(Object obj) {
            return this == obj;
        }
    }

    @Mock
    private FitnessFunction<TestSolution> ff1;

    @Mock
    private FitnessFunction<TestSolution> ff2;

    @BeforeEach
    void setUp() {
        lenient().when(ff1.isMinimizing()).thenReturn(true);
        lenient().when(ff2.isMinimizing()).thenReturn(true);
    }

    @Test
    void testBasicCrowdingDistance() {
        TestSolution s1 = spy(new TestSolution());
        TestSolution s2 = spy(new TestSolution());
        TestSolution s3 = spy(new TestSolution());

        doReturn(1.0).when(s1).getFitness(ff1);
        doReturn(2.0).when(s2).getFitness(ff1);
        doReturn(3.0).when(s3).getFitness(ff1);

        List<TestSolution> front = new ArrayList<>(List.of(s1, s2, s3));
        CrowdingDistanceSort<TestSolution> sorter = new CrowdingDistanceSort<>(List.of(ff1));

        sorter.calculateCrowdingDistanceAndSort(front);

        assertEquals(Double.MAX_VALUE, s1.getDistance());
        assertEquals(Double.MAX_VALUE, s3.getDistance());
        assertEquals(1.0, s2.getDistance());
    }

    @Test
    void testTwoObjectives() {
        TestSolution s1 = spy(new TestSolution());
        TestSolution s2 = spy(new TestSolution());
        TestSolution s3 = spy(new TestSolution());

        doReturn(1.0).when(s1).getFitness(ff1);
        doReturn(2.0).when(s2).getFitness(ff1);
        doReturn(3.0).when(s3).getFitness(ff1);

        doReturn(3.0).when(s1).getFitness(ff2);
        doReturn(2.0).when(s2).getFitness(ff2);
        doReturn(1.0).when(s3).getFitness(ff2);

        List<TestSolution> front = new ArrayList<>(List.of(s1, s2, s3));
        CrowdingDistanceSort<TestSolution> sorter = new CrowdingDistanceSort<>(List.of(ff1, ff2));

        sorter.calculateCrowdingDistanceAndSort(front);

        assertEquals(Double.MAX_VALUE, s1.getDistance());
        assertEquals(Double.MAX_VALUE, s3.getDistance());
        assertEquals(2.0, s2.getDistance());
    }

    @Test
    void testEqualFitness() {
        TestSolution s1 = spy(new TestSolution());
        TestSolution s2 = spy(new TestSolution());
        TestSolution s3 = spy(new TestSolution());

        doReturn(1.0).when(s1).getFitness(ff1);
        doReturn(1.0).when(s2).getFitness(ff1);
        doReturn(1.0).when(s3).getFitness(ff1);

        List<TestSolution> front = new ArrayList<>(List.of(s1, s2, s3));
        CrowdingDistanceSort<TestSolution> sorter = new CrowdingDistanceSort<>(List.of(ff1));

        sorter.calculateCrowdingDistanceAndSort(front);

        // s1 and s3 are boundaries in the sorted list (stable sort preserves order s1, s2, s3)
        // So s1 (index 0) and s3 (index 2) get MAX_VALUE.
        // s2 (index 1) gets 0.0.
        assertEquals(Double.MAX_VALUE, s1.getDistance());
        assertEquals(Double.MAX_VALUE, s3.getDistance());
        assertEquals(0.0, s2.getDistance());
    }
    
    @Test
    void testEmptyAndSingleList() {
        CrowdingDistanceSort<TestSolution> sorter = new CrowdingDistanceSort<>(List.of(ff1));
        
        List<TestSolution> empty = new ArrayList<>();
        sorter.calculateCrowdingDistanceAndSort(empty);
        assertEquals(0, empty.size());

        TestSolution s1 = spy(new TestSolution());
        doReturn(1.0).when(s1).getFitness(ff1);
        List<TestSolution> single = new ArrayList<>(List.of(s1));
        sorter.calculateCrowdingDistanceAndSort(single);
        assertEquals(1, single.size());
        assertEquals(Double.MAX_VALUE, s1.getDistance());
    }

    @Test
    void testSortingOrder() {
        TestSolution s1 = spy(new TestSolution());
        TestSolution s2 = spy(new TestSolution());
        TestSolution s3 = spy(new TestSolution());
        TestSolution s4 = spy(new TestSolution());

        // Setup fitness values to create distinct crowding distances
        // Range = 10.0 - 0.0 = 10.0
        doReturn(0.0).when(s1).getFitness(ff1);  // Boundary -> MAX_VALUE
        doReturn(1.0).when(s2).getFitness(ff1);  // Dist = (8.0 - 0.0) / 10.0 = 0.8
        doReturn(8.0).when(s3).getFitness(ff1);  // Dist = (10.0 - 1.0) / 10.0 = 0.9
        doReturn(10.0).when(s4).getFitness(ff1); // Boundary -> MAX_VALUE

        // Initial order: s2, s3, s1, s4 (mixed)
        List<TestSolution> front = new ArrayList<>(List.of(s2, s3, s1, s4));
        CrowdingDistanceSort<TestSolution> sorter = new CrowdingDistanceSort<>(List.of(ff1));

        sorter.calculateCrowdingDistanceAndSort(front);

        // Expected order:
        // 1. Boundaries (s1, s4) - order between them is stable/undefined but they come first
        // 2. s3 (0.9)
        // 3. s2 (0.8)

        // Check distances
        assertEquals(Double.MAX_VALUE, s1.getDistance());
        assertEquals(Double.MAX_VALUE, s4.getDistance());
        assertEquals(0.9, s3.getDistance(), 0.0001);
        assertEquals(0.8, s2.getDistance(), 0.0001);

        // Check list order
        // First two must be s1 and s4
        List<TestSolution> firstTwo = front.subList(0, 2);
        assertEquals(2, firstTwo.size());
        assert(firstTwo.contains(s1));
        assert(firstTwo.contains(s4));

        // Third must be s3
        assertEquals(s3, front.get(2));

        // Fourth must be s2
        assertEquals(s2, front.get(3));
    }
}
