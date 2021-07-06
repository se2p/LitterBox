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
package de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators.Crossover;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators.Mutation;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators.RefactorSequenceCrossover;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators.RefactorSequenceMutation;
import de.uni_passau.fim.se2.litterbox.utils.PropertyLoader;
import de.uni_passau.fim.se2.litterbox.utils.Randomness;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

class RefactorSequenceGeneratorTest {
    MockedStatic<Randomness> mockedRandomness;
    Program program;

    @BeforeEach
    void setupEnv() {
        mockedRandomness = Mockito.mockStatic(Randomness.class);
        program = mock(Program.class);
    }

    @AfterEach
    public void closeMocks() {
        mockedRandomness.close();
    }

    @Test
    void generateNewRefactorSequence() {

        final int initialProductionsPerSolution = PropertyLoader.getSystemIntProperty("nsga-ii.initialProductionsPerSolution");
        final int maxProductionNumber = PropertyLoader.getSystemIntProperty("nsga-ii.maxProductionNumber");

        Mutation<RefactorSequence> mutation = mock(RefactorSequenceMutation.class);
        Crossover<RefactorSequence> crossover = mock(RefactorSequenceCrossover.class);

        List<Integer> expectedProductions = List.of(1, 2);

        // returning 1 means 2 ints will be included since the formula uses (1 + random Number) to avoid 0
        mockedRandomness.when(() -> Randomness.nextInt(initialProductionsPerSolution)).thenReturn(1);

        // include the integers 1 and 2
        mockedRandomness.when(() -> Randomness.nextInt(maxProductionNumber)).thenReturn(1).thenReturn(2);

        RefactorSequenceGenerator generator = new RefactorSequenceGenerator(program, mutation, crossover, List.of());
        RefactorSequence generated = generator.get();

        assertEquals(2, generated.getProductions().size());
        assertEquals(expectedProductions, generated.getProductions());
        assertSame(mutation, generated.getMutation());
        assertSame(crossover, generated.getCrossover());
    }
}
