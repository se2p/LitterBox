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
        MaximizingFitnessFunction<RefactorSequence>  numberOfHelloBocks = new NumberOfHelloBlocks();
        RefactorSequence refactorSequence = mock(RefactorSequence.class);
        when(refactorSequence.getRefactoredProgram()).thenReturn(program);

        Assertions.assertEquals(2, numberOfHelloBocks.getFitness(refactorSequence));
    }
}
