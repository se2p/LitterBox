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
package de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes;

import de.uni_passau.fim.se2.litterbox.analytics.RefactoringFinder;
import de.uni_passau.fim.se2.litterbox.analytics.refactorings.MergeDoubleIfFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators.Crossover;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators.Mutation;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators.RefactorSequenceCrossover;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators.RefactorSequenceMutation;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.MergeDoubleIf;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.Refactoring;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RefactorSequenceTest {

    Program program;
    Mutation<RefactorSequence> mutation;
    Crossover<RefactorSequence> crossover;
    List<Integer> productions;
    RefactoringFinder refactoringFinder;
    List<RefactoringFinder> refactoringFinders;
    RefactorSequence refactorSequence;

    @BeforeEach
    void setupEnv() {
        program = mock(Program.class);
        mutation = mock(RefactorSequenceMutation.class);
        crossover = mock(RefactorSequenceCrossover.class);
        productions = List.of(0, 1, 2);
        refactoringFinder = mock(MergeDoubleIfFinder.class);
        refactoringFinders = List.of(refactoringFinder);

        refactorSequence = new RefactorSequence(program, mutation, crossover, productions, refactoringFinders);
    }

    @Test
    void applySequenceToProgram() {
        Refactoring refactoring1 = mock(MergeDoubleIf.class);
        when(refactoring1.apply(program)).thenReturn(program);
        Refactoring refactoring2 = mock(MergeDoubleIf.class);
        when(refactoring2.apply(program)).thenReturn(program);

        List<Refactoring> possibleRefactorings = List.of(refactoring1, refactoring2);
        when(refactoringFinder.check(program)).thenReturn(possibleRefactorings);

        refactorSequence.getRefactoredProgram();

        assertEquals(List.of(refactoring1, refactoring2, refactoring1), refactorSequence.getExecutedRefactorings());
    }

    @Test
    void copyCreatesDeepCopy() {
        RefactorSequence copy = refactorSequence.copy();
        assertNotSame(refactorSequence, copy);
        assertEquals(refactorSequence, copy);

        // change the first element of the production list
        copy.getProductions().add(0, 1);
        copy.getProductions().remove(1);
        copy.getExecutedRefactorings().add(mock(MergeDoubleIf.class));

        assertNotSame(refactorSequence, copy);
        assertNotSame(copy.getProductions().get(0), refactorSequence.getProductions().get(0));
        assertNotEquals(copy.getExecutedRefactorings(), refactorSequence.getExecutedRefactorings());
    }

    @Test
    void hashCodeChangesWithObject() {
        Program other = mock(Program.class);

        Refactoring refactoring1 = mock(MergeDoubleIf.class);
        when(refactoring1.apply(any())).thenReturn(other);
        Refactoring refactoring2 = mock(MergeDoubleIf.class);
        when(refactoring2.apply(any())).thenReturn(other);

        List<Refactoring> possibleRefactorings = List.of(refactoring1, refactoring2);
        when(refactoringFinder.check(program)).thenReturn(possibleRefactorings);

        RefactorSequence emptySequence = refactorSequence.copy();
        assertEquals(refactorSequence, emptySequence);
        assertEquals(refactorSequence.hashCode(), emptySequence.hashCode());

        emptySequence.getProductions().clear();
        emptySequence.getExecutedRefactorings().clear();
        emptySequence = emptySequence.copy(); // force re-creation of the refactoredProgram supplier
        assertNotEquals(refactorSequence.hashCode(), emptySequence.hashCode());
    }
}
