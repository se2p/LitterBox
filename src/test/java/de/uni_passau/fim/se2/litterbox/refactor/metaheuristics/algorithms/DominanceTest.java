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

import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.Solution;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions.FitnessFunction;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators.Crossover;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators.Mutation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DominanceTest {

    // Concrete implementation to satisfy self-referential generic bound
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
            return 0;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof TestSolution;
        }
    }

    @Mock
    private FitnessFunction<TestSolution> ffMin;

    @Mock
    private FitnessFunction<TestSolution> ffMax;

    @Mock
    private TestSolution s1;

    @Mock
    private TestSolution s2;

    @BeforeEach
    void setUp() {
        // Default behavior: ffMin is minimizing, ffMax is maximizing
        lenient().when(ffMin.isMinimizing()).thenReturn(true);
        lenient().when(ffMax.isMinimizing()).thenReturn(false);
    }

    @Test
    void testDominanceMinimizing() {
        Dominance<TestSolution> dominance = new Dominance<>(List.of(ffMin));

        // S1 dominates S2 (1.0 < 2.0)
        when(s1.getFitness(ffMin)).thenReturn(1.0);
        when(s2.getFitness(ffMin)).thenReturn(2.0);
        assertTrue(dominance.test(s1, s2));

        // S1 does not dominate S2 (2.0 > 1.0)
        when(s1.getFitness(ffMin)).thenReturn(2.0);
        when(s2.getFitness(ffMin)).thenReturn(1.0);
        assertFalse(dominance.test(s1, s2));
    }

    @Test
    void testDominanceMaximizing() {
        Dominance<TestSolution> dominance = new Dominance<>(List.of(ffMax));

        // S1 dominates S2 (2.0 > 1.0)
        when(s1.getFitness(ffMax)).thenReturn(2.0);
        when(s2.getFitness(ffMax)).thenReturn(1.0);
        assertTrue(dominance.test(s1, s2));

        // S1 does not dominate S2 (1.0 < 2.0)
        when(s1.getFitness(ffMax)).thenReturn(1.0);
        when(s2.getFitness(ffMax)).thenReturn(2.0);
        assertFalse(dominance.test(s1, s2));
    }

    @Test
    void testDominanceMixed() {
        Dominance<TestSolution> dominance = new Dominance<>(List.of(ffMin, ffMax));

        // S1 dominates S2: better in both
        // ffMin: 1.0 < 2.0 (better)
        // ffMax: 2.0 > 1.0 (better)
        when(s1.getFitness(ffMin)).thenReturn(1.0);
        when(s2.getFitness(ffMin)).thenReturn(2.0);
        when(s1.getFitness(ffMax)).thenReturn(2.0);
        when(s2.getFitness(ffMax)).thenReturn(1.0);
        assertTrue(dominance.test(s1, s2));

        // S1 dominates S2: equal in one, better in other
        // ffMin: 1.0 == 1.0 (equal)
        // ffMax: 2.0 > 1.0 (better)
        when(s1.getFitness(ffMin)).thenReturn(1.0);
        when(s2.getFitness(ffMin)).thenReturn(1.0);
        when(s1.getFitness(ffMax)).thenReturn(2.0);
        when(s2.getFitness(ffMax)).thenReturn(1.0);
        assertTrue(dominance.test(s1, s2));
    }

    @Test
    void testNoDominanceEqual() {
        Dominance<TestSolution> dominance = new Dominance<>(List.of(ffMin));

        // Equal fitness -> no dominance
        when(s1.getFitness(ffMin)).thenReturn(1.0);
        when(s2.getFitness(ffMin)).thenReturn(1.0);
        assertFalse(dominance.test(s1, s2));
    }

    @Test
    void testNoDominanceWorse() {
        Dominance<TestSolution> dominance = new Dominance<>(List.of(ffMin, ffMax));

        // S1 worse in one -> no dominance
        // ffMin: 1.0 < 2.0 (better)
        // ffMax: 1.0 < 2.0 (worse)
        when(s1.getFitness(ffMin)).thenReturn(1.0);
        when(s2.getFitness(ffMin)).thenReturn(2.0);
        when(s1.getFitness(ffMax)).thenReturn(1.0);
        when(s2.getFitness(ffMax)).thenReturn(2.0);
        assertFalse(dominance.test(s1, s2));
    }
}
