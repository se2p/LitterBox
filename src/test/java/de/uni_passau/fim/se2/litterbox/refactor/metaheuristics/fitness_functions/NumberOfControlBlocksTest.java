package de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions;


import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.RefactorSequence;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NumberOfControlBlocksTest implements JsonTest{

    @Test
    void testGetFitnessCorrectly() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/refactoring/forLoopWithHelloBlock.json");

        MinimizingFitnessFunction<RefactorSequence>  numberOfControlBocks = new NumberOfControlBlocks();
        RefactorSequence refactorSequence = mock(RefactorSequence.class);
        when(refactorSequence.getRefactoredProgram()).thenReturn(program);

        Assertions.assertEquals(1, numberOfControlBocks.getFitness(refactorSequence));
    }
}
