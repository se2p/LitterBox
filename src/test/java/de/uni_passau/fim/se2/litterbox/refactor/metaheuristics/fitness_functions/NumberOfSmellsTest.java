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
import static org.mockito.ArgumentMatchers.anyBoolean;
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
