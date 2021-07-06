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

import de.uni_passau.fim.se2.litterbox.analytics.RefactoringFinder;
import de.uni_passau.fim.se2.litterbox.analytics.refactorings.MergeDoubleIfFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.RefactorSequence;
import de.uni_passau.fim.se2.litterbox.utils.Pair;
import de.uni_passau.fim.se2.litterbox.utils.Randomness;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class RefactorSequenceCrossoverTest {
    MockedStatic<Randomness> mockedRandomness;
    Program program;
    Mutation<RefactorSequence> mutation;
    RefactorSequenceCrossover crossover;
    List<RefactoringFinder> refactoringFinders;

    @BeforeEach
    void setupEnv() {
        mockedRandomness = Mockito.mockStatic(Randomness.class);
        program = mock(Program.class);
        mutation = mock(RefactorSequenceMutation.class);
        crossover = new RefactorSequenceCrossover();
        refactoringFinders = List.of(new MergeDoubleIfFinder());
    }

    @AfterEach
    public void closeMocks() {
        mockedRandomness.close();
    }


    @Test
    void testCrossover() {

        List<Integer> production1 = List.of(0, 0, 0);
        List<Integer> production2 = List.of(1, 1, 1);

        RefactorSequence parent1 = new RefactorSequence(program, mutation, crossover, production1, refactoringFinders);
        RefactorSequence parent2 = new RefactorSequence(program, mutation, crossover, production2, refactoringFinders);

        mockedRandomness.when(() -> Randomness.nextInt(2)).thenReturn(1);
        Pair<RefactorSequence> children = parent1.crossover(parent2);

        assertEquals(0, children.getFst().getProductions().get(0));
        assertEquals(0, children.getFst().getProductions().get(1));
        assertEquals(1, children.getFst().getProductions().get(2));

        assertEquals(1, children.getSnd().getProductions().get(0));
        assertEquals(1, children.getSnd().getProductions().get(1));
        assertEquals(0, children.getSnd().getProductions().get(2));
    }

    @Test
    void testCrossoverSmallProductions() {

        List<Integer> production1 = List.of(0, 0);
        List<Integer> production2 = List.of(1);

        RefactorSequence parent1 = new RefactorSequence(program, mutation, crossover, production1, refactoringFinders);
        RefactorSequence parent2 = new RefactorSequence(program, mutation, crossover, production2, refactoringFinders);

        mockedRandomness.when(() -> Randomness.nextInt(1)).thenReturn(0);
        Pair<RefactorSequence> children = parent1.crossover(parent2);

        assertEquals(0, children.getFst().getProductions().get(0));

        assertEquals(1, children.getSnd().getProductions().get(0));
        assertEquals(0, children.getSnd().getProductions().get(1));
    }
}
