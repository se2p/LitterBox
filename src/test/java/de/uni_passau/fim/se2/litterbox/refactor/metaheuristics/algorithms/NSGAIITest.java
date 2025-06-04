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
package de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.algorithms;

import com.google.common.collect.Lists;
import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.RefactoringFinder;
import de.uni_passau.fim.se2.litterbox.analytics.refactorings.BlockFinder;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.ControlStmt;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.*;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions.FitnessFunction;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions.NumberOfControlBlocks;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions.NumberOfHelloBlocks;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators.*;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.DeleteControlBlock;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.Refactoring;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class NSGAIITest implements JsonTest {

    @BeforeEach
    void setupEnv() {
        System.setProperty("generations", "2");
    }

    @Test
    @SuppressWarnings("unchecked")
    void testNSGAIICalls() {
        RefactorSequence c1gen1 = mock(RefactorSequence.class);
        RefactorSequence c2gen1 = mock(RefactorSequence.class);
        RefactorSequence c3gen1 = mock(RefactorSequence.class);
        RefactorSequence c4gen1 = mock(RefactorSequence.class);
        List<RefactorSequence> gen1 = Lists.newArrayList(c1gen1, c2gen1, c3gen1, c4gen1);

        RefactorSequence c1gen2 = mock(RefactorSequence.class);
        RefactorSequence c2gen2 = mock(RefactorSequence.class);
        RefactorSequence c3gen2 = mock(RefactorSequence.class);
        RefactorSequence c4gen2 = mock(RefactorSequence.class);
        List<RefactorSequence> gen2 = Lists.newArrayList(c1gen2, c2gen2, c3gen2, c4gen2);

        List<RefactorSequence> combined = Lists.newLinkedList(gen1);
        combined.addAll(gen2);

        List<RefactorSequence> front1 = List.of(c1gen1, c1gen2);
        List<RefactorSequence> front2 = List.of(c2gen1, c2gen2);
        List<RefactorSequence> front3 = List.of(c3gen1, c3gen2);
        List<RefactorSequence> front4 = List.of(c4gen1, c4gen2);

        List<List<RefactorSequence>> paretoFronts = Lists.newArrayList(front1, front2, front3, front4);

        FixedSizePopulationGenerator<RefactorSequence> populationGenerator = mock(FixedSizePopulationGenerator.class);
        when(populationGenerator.getPopulationSize()).thenReturn(4);
        when((populationGenerator.get())).thenReturn(gen1);

        OffspringGenerator<RefactorSequence> offspringGenerator = mock(OffspringGenerator.class);
        when(offspringGenerator.generateOffspring(gen1)).thenReturn(gen2);

        FastNonDominatedSort<RefactorSequence> fastNonDominatedSort = mock(FastNonDominatedSort.class);
        when(fastNonDominatedSort.fastNonDominatedSort(combined)).thenReturn(paretoFronts);

        CrowdingDistanceSort<RefactorSequence> crowdingDistanceSort = mock(CrowdingDistanceSort.class);
        doNothing().when(crowdingDistanceSort).calculateCrowdingDistanceAndSort(anyList());

        List<RefactorSequence> populationAfterNSGAII = Lists.newArrayList(c1gen1, c1gen2, c2gen1, c2gen2);
        ignoreStubs(offspringGenerator);

        List<List<RefactorSequence>> finalFronts = List.of(front1, front2);
        when(fastNonDominatedSort.fastNonDominatedSort(populationAfterNSGAII)).thenReturn(finalFronts);

        GeneticAlgorithm<RefactorSequence> nsgaii = new NSGAII<>(populationGenerator, offspringGenerator, fastNonDominatedSort, crowdingDistanceSort);
        List<RefactorSequence> nsgaiiSolution = nsgaii.findSolution();
        assertEquals(2, nsgaiiSolution.size());
        assertEquals(front1, nsgaiiSolution);
    }

    @Test
    @SuppressWarnings("unchecked")
    void evolveOnEmptyPopulation() {
        List<RefactorSequence> emptyList = List.of();

        FixedSizePopulationGenerator<RefactorSequence> populationGenerator = mock(FixedSizePopulationGenerator.class);
        OffspringGenerator<RefactorSequence> offspringGenerator = mock(OffspringGenerator.class);
        when(offspringGenerator.generateOffspring(emptyList)).thenReturn(emptyList);

        FastNonDominatedSort<RefactorSequence> fastNonDominatedSort = mock(FastNonDominatedSort.class);
        CrowdingDistanceSort<RefactorSequence> crowdingDistanceSort = mock(CrowdingDistanceSort.class);

        when(fastNonDominatedSort.fastNonDominatedSort(emptyList)).thenReturn(List.of());
        NSGAII<RefactorSequence> nsgaii = new NSGAII<>(populationGenerator, offspringGenerator, fastNonDominatedSort, crowdingDistanceSort);
        assertTrue(nsgaii.evolve(emptyList).isEmpty());
    }

    @Test
    void testNSGAIIWithDummyRefactorings() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/refactoring/helloBlockHelloBlockWithinControl.json");
        Program expectedProgram = getAST("src/test/fixtures/refactoring/helloBlock.json");

        List<RefactoringFinder> refactorings = new ArrayList<>();
        refactorings.add(new BlockFinder());
        int populationSize = 5;

        List<Stmt> stmtListProgram = getStmtListOfProgram(program);
        List<String> stmtNamesProgram = stmtListProgram.stream().map(Stmt::getUniqueName).toList();
        List<String> stmtNamesExpectedProgram = getStmtListOfProgram(expectedProgram).stream().map(Stmt::getUniqueName).toList();
        ControlStmt controlStmt = (ControlStmt) stmtListProgram.get(1);
        Refactoring deleteControlBlock = new DeleteControlBlock(controlStmt);

        NumberOfHelloBlocks numberOfHelloBlocks = new NumberOfHelloBlocks();
        NumberOfControlBlocks numberOfControlBlocks = new NumberOfControlBlocks();

        List<String> expectedRefactorings = List.of(deleteControlBlock.getName());

        GeneticAlgorithm<RefactorSequence> nsgaii = initializeNSGAII(program, refactorings, populationSize);
        List<RefactorSequence> solutions = nsgaii.findSolution();

        for (RefactorSequence solution : solutions) {
            List<String> actualRefactorings = solution.getExecutedRefactorings().stream().map(Refactoring::getName).toList();

            if (actualRefactorings.size() > 0) {
                assertEquals(expectedRefactorings, actualRefactorings);

                Program refactored = solution.getRefactoredProgram();
                List<String> stmtNamesRefactoredProgram = getStmtListOfProgram(refactored).stream().map(Stmt::getUniqueName).toList();
                assertEquals(stmtNamesExpectedProgram, stmtNamesRefactoredProgram);

                assertEquals(0, numberOfControlBlocks.getFitness(solution));
                assertEquals(1, numberOfHelloBlocks.getFitness(solution));
            } else {
                Program refactored = solution.getRefactoredProgram();
                List<String> stmtNamesRefactoredProgram = getStmtListOfProgram(refactored).stream().map(Stmt::getUniqueName).toList();
                assertEquals(stmtNamesProgram, stmtNamesRefactoredProgram);

                assertEquals(1, numberOfControlBlocks.getFitness(solution));
                assertEquals(2, numberOfHelloBlocks.getFitness(solution));
            }
        }
    }

    private static List<Stmt> getStmtListOfProgram(Program program) {
        return program.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().get(0).getStmtList().getStmts();
    }

    private static NSGAII<RefactorSequence> initializeNSGAII(Program program, List<RefactoringFinder> refactorings, int populationSize) {
        Crossover<RefactorSequence> crossover = new RefactorSequenceCrossover();
        Mutation<RefactorSequence> mutation = new RefactorSequenceMutation(refactorings);

        ChromosomeGenerator<RefactorSequence> chromosomeGenerator = new RefactorSequenceGenerator(program, mutation, crossover, refactorings);

        FixedSizePopulationGenerator<RefactorSequence> populationGenerator = new FixedSizePopulationGenerator<>(chromosomeGenerator, populationSize);
        BinaryRankTournament<RefactorSequence> binaryRankTournament = new BinaryRankTournament<>();
        OffspringGenerator<RefactorSequence> offspringGenerator = new OffspringGenerator<>(binaryRankTournament);

        List<FitnessFunction<RefactorSequence>> fitnessFunctions = new LinkedList<>();

        NumberOfHelloBlocks numberOfHelloBlocks = new NumberOfHelloBlocks();
        NumberOfControlBlocks numberOfControlBlocks = new NumberOfControlBlocks();

        fitnessFunctions.add(numberOfHelloBlocks);
        fitnessFunctions.add(numberOfControlBlocks);
        FastNonDominatedSort<RefactorSequence> fastNonDominatedSort = new FastNonDominatedSort<>(fitnessFunctions);
        CrowdingDistanceSort<RefactorSequence> crowdingDistanceSort = new CrowdingDistanceSort<>(fitnessFunctions);
        return new NSGAII<>(populationGenerator, offspringGenerator, fastNonDominatedSort, crowdingDistanceSort);
    }
}
