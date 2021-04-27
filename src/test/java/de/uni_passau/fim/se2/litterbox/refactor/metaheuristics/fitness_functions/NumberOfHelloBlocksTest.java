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

public class NumberOfHelloBlocksTest implements JsonTest{

    @Test
    void testGetFitnessCorrectly() throws IOException, ParsingException {
        Program program = getAST("./src/test/fixtures/bugpattern/missingPenDown.json");
        MinimizingFitnessFunction<RefactorSequence>  numberOfHelloBocks = new NumberOfHelloBlocks(program);
        RefactorSequence refactorSequence = mock(RefactorSequence.class);
        when(refactorSequence.applyToProgram(program)).thenReturn(program);

        Assertions.assertEquals(2, numberOfHelloBocks.getFitness(refactorSequence));
    }
}
