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
package de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators;

import com.google.common.collect.Lists;
import de.uni_passau.fim.se2.litterbox.analytics.RefactoringFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.RefactorSequence;
import de.uni_passau.fim.se2.litterbox.utils.Randomness;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.mockito.Mockito.mock;

class BinaryRankTournamentTest {
    MockedStatic<Randomness> mockedRandomness;

    Program program;

    Mutation<RefactorSequence> mutation;
    Crossover<RefactorSequence> crossover;

    List<Integer> productions;
    List<RefactoringFinder> refactoringFinders;

    RefactorSequence refactorSequence1;
    RefactorSequence refactorSequence2;

    List<RefactorSequence> population;

    Selection<RefactorSequence> selection;

    @BeforeEach
    void setupEnv() {
        mockedRandomness = Mockito.mockStatic(Randomness.class);

        program = mock(Program.class);

        mutation = mock(RefactorSequenceMutation.class);
        crossover = mock(RefactorSequenceCrossover.class);

        productions = List.of(1, 1, 1);
        refactoringFinders = List.of();

        refactorSequence1 = new RefactorSequence(program, mutation, crossover, productions, refactoringFinders);
        refactorSequence2 = new RefactorSequence(program, mutation, crossover, productions, refactoringFinders);

        population = Lists.newArrayList(refactorSequence1, refactorSequence2);

        selection = new BinaryRankTournament<>();
    }

    @AfterEach
    public void closeMocks() {
        mockedRandomness.close();
    }

    @Test
    void testSelectionByRank() {
        refactorSequence1.setRank(0);
        refactorSequence2.setRank(1);

        mockedRandomness.when(() -> Randomness.nextInt(2)).thenReturn(0).thenReturn(1);
        RefactorSequence candidate1 = selection.apply(population);
        assertNotSame(refactorSequence1, candidate1);
        assertEquals(refactorSequence1, candidate1);

        refactorSequence1.setRank(2);
        mockedRandomness.when(() -> Randomness.nextInt(2)).thenReturn(0).thenReturn(1);
        RefactorSequence candidate2 = selection.apply(population);
        assertNotSame(refactorSequence2, candidate2);
        assertEquals(refactorSequence2, candidate2);
    }

    @Test
    void testSelectionByDistance() {
        refactorSequence1.setRank(0);
        refactorSequence2.setRank(0);
        refactorSequence1.setDistance(0.0);
        refactorSequence2.setDistance(1.0);
        mockedRandomness.when(() -> Randomness.nextInt(2)).thenReturn(0).thenReturn(1);

        RefactorSequence candidate1 = selection.apply(population);
        assertNotSame(refactorSequence2, candidate1);
        assertEquals(refactorSequence2, candidate1);

        refactorSequence1.setDistance(2.0);
        mockedRandomness.when(() -> Randomness.nextInt(2)).thenReturn(0).thenReturn(1);
        RefactorSequence candidate2 = selection.apply(population);
        assertNotSame(refactorSequence1, candidate1);
        assertEquals(refactorSequence1, candidate1);
    }

    @Test
    void testNeverTheSameBothArePicked() {
        refactorSequence1.setRank(0);
        refactorSequence2.setRank(1);
        mockedRandomness.when(() -> Randomness.nextInt(2)).thenReturn(0).thenReturn(0).thenReturn(1);
        RefactorSequence candidate = selection.apply(population);
        assertNotSame(refactorSequence2, candidate);
        assertEquals(refactorSequence2, candidate);
    }
}
