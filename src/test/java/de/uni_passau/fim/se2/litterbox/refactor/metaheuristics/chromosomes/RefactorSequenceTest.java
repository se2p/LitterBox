/*
 * Copyright (C) 2019-2026 LitterBox contributors
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
        copy.getProductions().addFirst(1);
        copy.getProductions().remove(1);
        copy.getExecutedRefactorings().add(mock(MergeDoubleIf.class));

        assertNotSame(refactorSequence, copy);
        assertNotSame(copy.getProductions().getFirst(), refactorSequence.getProductions().getFirst());
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
    @Test
    void testEqualityContract() {
        // Reflexivity
        assertEquals(refactorSequence, refactorSequence);

        // Null check
        assertNotEquals(null, refactorSequence);

        // Type check
        assertNotEquals(new Object(), refactorSequence);

        // Symmetry
        RefactorSequence copy = refactorSequence.copy();
        assertEquals(refactorSequence, copy);
        assertEquals(copy, refactorSequence);
    }

    @Test
    void testEqualityBasedOnProductions() {
        // Setup refactorings to produce different programs
        Program p1 = mock(Program.class);
        Program p2 = mock(Program.class);
        Program p3 = mock(Program.class);
        
        Refactoring r1 = mock(Refactoring.class);
        when(r1.apply(any())).thenReturn(p1);
        Refactoring r2 = mock(Refactoring.class);
        when(r2.apply(any())).thenReturn(p2);
        Refactoring r3 = mock(Refactoring.class);
        when(r3.apply(any())).thenReturn(p3);
        
        when(refactoringFinder.check(any())).thenReturn(List.of(r1, r2, r3));

        RefactorSequence seq1 = new RefactorSequence(program, mutation, crossover, List.of(0), refactoringFinders);
        RefactorSequence seq2 = new RefactorSequence(program, mutation, crossover, List.of(0), refactoringFinders);
        RefactorSequence seq3 = new RefactorSequence(program, mutation, crossover, List.of(1), refactoringFinders);

        // Should be equal based on productions (executedRefactorings is null)
        assertEquals(seq1, seq2);
        
        // Should not be equal due to different productions resulting in different programs
        assertNotEquals(seq1, seq3);
    }

    @Test
    void testEqualityBasedOnPhenotype() {
        // Setup: Two sequences with different productions but same resulting program
        Program resultProgram = mock(Program.class);
        
        // Mock refactoring finder to return refactorings that produce the SAME program
        Refactoring r1 = mock(Refactoring.class);
        when(r1.apply(any())).thenReturn(resultProgram);
        Refactoring r2 = mock(Refactoring.class);
        when(r2.apply(any())).thenReturn(resultProgram);
        
        when(refactoringFinder.check(any())).thenReturn(List.of(r1, r2));

        // Sequence 1: Productions [0] -> r1 -> resultProgram
        RefactorSequence seq1 = new RefactorSequence(program, mutation, crossover, List.of(0), refactoringFinders);
        
        // Sequence 2: Productions [1] -> r2 -> resultProgram
        RefactorSequence seq2 = new RefactorSequence(program, mutation, crossover, List.of(1), refactoringFinders);

        // Should be equal because they result in the same phenotype, even though genotypes differ.
        // The equals method will compute the phenotype because genotypes differ.
        assertEquals(seq1, seq2, "Should be equal based on same phenotype");
    }
}
