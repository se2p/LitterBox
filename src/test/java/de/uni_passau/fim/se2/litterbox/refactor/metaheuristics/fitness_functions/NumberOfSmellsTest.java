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
package de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions;

import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.smells.DoubleIf;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.RefactorSequence;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class NumberOfSmellsTest {
    IssueFinder mockedFinder = mock(DoubleIf.class);
    List<IssueFinder> issueFinders = List.of(mockedFinder);
    Program program = mock(Program.class);

    @Test
    void testSizeEvaluation() {
        doNothing().when(mockedFinder).setIgnoreLooseBlocks(anyBoolean());
        when(mockedFinder.check(program)).thenReturn(Set.of(mock(Issue.class)));
        MinimizingFitnessFunction<RefactorSequence> fitnessFunction = new NumberOfSmells(issueFinders, false);
        assertTrue(fitnessFunction.isMinimizing());
        RefactorSequence refactorSequence = mock(RefactorSequence.class);
        when(refactorSequence.getRefactoredProgram()).thenReturn(program);

        assertEquals(1, fitnessFunction.getFitness(refactorSequence));
    }
}
