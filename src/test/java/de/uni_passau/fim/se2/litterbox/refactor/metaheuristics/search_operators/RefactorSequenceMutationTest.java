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
package de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators;

import de.uni_passau.fim.se2.litterbox.analytics.RefactoringFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.RefactorSequence;
import de.uni_passau.fim.se2.litterbox.utils.PropertyLoader;
import de.uni_passau.fim.se2.litterbox.utils.Randomness;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class RefactorSequenceMutationTest {
    MockedStatic<Randomness> mockedRandomness;
    Program program;
    Mutation<RefactorSequence> mutation;
    Crossover<RefactorSequence> crossover;
    List<RefactoringFinder> refactoringFinders;

    @BeforeEach
    void setupEnv() {
        mockedRandomness = Mockito.mockStatic(Randomness.class);

        program = mock(Program.class);
        refactoringFinders = List.of();
        mutation = new RefactorSequenceMutation(refactoringFinders);
        crossover = mock(RefactorSequenceCrossover.class);
    }

    @AfterEach
    public void closeMocks() {
        mockedRandomness.close();
    }

    @Test
    void testMutationInsert() {
        final int NUMBER_OF_POSSIBLE_PRODUCTIONS = PropertyLoader.getSystemIntProperty("nsga-ii.maxProductionNumber");
        
        // Mock randomness to:
        // 1. Trigger mutation for the first element (nextDouble < 1/size)
        // 2. Select mutation type 0 (Insert)
        // 3. Select new production value (e.g., 42)
        // 4. For subsequent elements, do not trigger mutation (nextDouble >= 1/size)
        
        // Initial list: [1, 2]
        // Size: 2. pMutate = 0.5.
        // Iteration 0 (index 0): nextDouble() returns 0.1 (< 0.5) -> Mutate
        //   mutateAtIndex: nextInt(3) returns 0 (Insert)
        //   nextInt(MAX) returns 42
        //   List becomes [42, 1, 2]. Index increments to 1.
        // Loop increments index to 2.
        // Iteration 1 (index 2): nextDouble() returns 0.9 (> 0.5) -> No mutate
        // Loop increments index to 3.
        // Size is 3. Loop terminates.
        
        mockedRandomness.when(Randomness::nextDouble).thenReturn(0.1).thenReturn(0.9);
        mockedRandomness.when(() -> Randomness.nextInt(3)).thenReturn(0); // Insert
        mockedRandomness.when(() -> Randomness.nextInt(NUMBER_OF_POSSIBLE_PRODUCTIONS)).thenReturn(42);

        List<Integer> productions = List.of(1, 2);
        RefactorSequence parent = new RefactorSequence(program, mutation, crossover, productions, refactoringFinders);

        RefactorSequence mutant = parent.mutate();
        
        List<Integer> mutatedProductions = mutant.getProductions();
        assertEquals(3, mutatedProductions.size());
        assertEquals(42, mutatedProductions.getFirst());
        assertEquals(1, mutatedProductions.get(1));
        assertEquals(2, mutatedProductions.get(2));
    }

    @Test
    void testMutationReplace() {
        final int NUMBER_OF_POSSIBLE_PRODUCTIONS = PropertyLoader.getSystemIntProperty("nsga-ii.maxProductionNumber");
        
        // Mock randomness to:
        // 1. Trigger mutation for the first element
        // 2. Select mutation type 1 (Replace)
        // 3. Select new production value (e.g., 99)
        // 4. No further mutations
        
        mockedRandomness.when(Randomness::nextDouble).thenReturn(0.1).thenReturn(0.9);
        mockedRandomness.when(() -> Randomness.nextInt(3)).thenReturn(1); // Replace
        mockedRandomness.when(() -> Randomness.nextInt(NUMBER_OF_POSSIBLE_PRODUCTIONS)).thenReturn(99);

        List<Integer> productions = List.of(1, 2);
        RefactorSequence parent = new RefactorSequence(program, mutation, crossover, productions, refactoringFinders);

        RefactorSequence mutant = parent.mutate();
        
        List<Integer> mutatedProductions = mutant.getProductions();
        assertEquals(2, mutatedProductions.size());
        assertEquals(99, mutatedProductions.getFirst());
        assertEquals(2, mutatedProductions.get(1));
    }

    @Test
    void testMutationDelete() {
        // Mock randomness to:
        // 1. Trigger mutation for the first element
        // 2. Select mutation type 2 (Delete)
        // 3. No further mutations
        
        // Initial: [1, 2]
        // Index 0: Mutate -> Delete. List becomes [2]. Index decrements to -1.
        // Loop increments index to 0.
        // Index 0 (was 1): nextDouble() returns 0.9 -> No mutate.
        // Loop increments index to 1.
        // Size is 1. Loop terminates.
        
        mockedRandomness.when(Randomness::nextDouble).thenReturn(0.1).thenReturn(0.9);
        mockedRandomness.when(() -> Randomness.nextInt(3)).thenReturn(2); // Delete

        List<Integer> productions = List.of(1, 2);
        RefactorSequence parent = new RefactorSequence(program, mutation, crossover, productions, refactoringFinders);

        RefactorSequence mutant = parent.mutate();
        
        List<Integer> mutatedProductions = mutant.getProductions();
        assertEquals(1, mutatedProductions.size());
        assertEquals(2, mutatedProductions.getFirst());
    }
}
