package de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.algorithms;

import com.google.common.collect.Lists;
import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.RefactoringFinder;
import de.uni_passau.fim.se2.litterbox.analytics.refactorings.BlockFinder;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.*;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions.FitnessFunction;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions.NumberOfControlBlocks;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions.NumberOfHelloBlocks;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators.*;
import de.uni_passau.fim.se2.litterbox.report.ConsoleRefactorReportGenerator;
import de.uni_passau.fim.se2.litterbox.utils.PropertyLoader;
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
        PropertyLoader.setDefaultSystemProperties("nsga-ii.properties");
    }

    @Test
    void testNSGAIICalls() {
        int generations = 2;

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

        GeneticAlgorithm<RefactorSequence> nsgaii = new NSGAII<>(populationGenerator, offspringGenerator, fastNonDominatedSort, crowdingDistanceSort, generations);
        List<RefactorSequence> nsgaiiSolution = nsgaii.findSolution();
        assertEquals(2, nsgaiiSolution.size());
        assertEquals(front1, nsgaiiSolution);
    }

    @Test
    void testNSGAIIWithDummyRefactorings() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/refactoring/helloBlockHelloBlockWithinControl.json");
        Program programToCheck = getAST("src/test/fixtures/refactoring/onlyTwoHelloBlocks.json");

        List<RefactoringFinder> refactorings = new ArrayList<>();
        refactorings.add(new BlockFinder());
        int maxGen = 10;
        int populationSize = 5;


        Crossover<RefactorSequence> crossover = new RefactorSequenceCrossover();
        Mutation<RefactorSequence> mutation = new RefactorSequenceMutation(refactorings);

        ChromosomeGenerator<RefactorSequence> chromosomeGenerator = new RefactorSequenceGenerator(program.deepCopy(), mutation, crossover, refactorings);

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

        GeneticAlgorithm<RefactorSequence> nsgaii = new NSGAII<>(populationGenerator, offspringGenerator, fastNonDominatedSort, crowdingDistanceSort, maxGen);
        List<RefactorSequence> solutions = nsgaii.findSolution();
        for (RefactorSequence solution : solutions) {
            Program refactored = solution.getRefactoredProgram();
            Script refactoredScript = refactored.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().get(0);
            List<Stmt> refactoredStmtList = refactoredScript.getStmtList().getStmts();
            System.out.println("size:" + refactoredStmtList.size());

            ConsoleRefactorReportGenerator reportGenerator = new ConsoleRefactorReportGenerator();
            reportGenerator.generateReport(program, solution.getExecutedRefactorings());
        }
//        Script refactoredScript2 = programToCheck.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().get(0);
//        List<Stmt> refactoredStmtList2 = refactoredScript2.getStmtList().getStmts();
    }

    @Test
    void evolveOnEmptyPopulation() {
        List<RefactorSequence> emptyList = List.of();

        FixedSizePopulationGenerator<RefactorSequence> populationGenerator = mock(FixedSizePopulationGenerator.class);
        OffspringGenerator<RefactorSequence> offspringGenerator = mock(OffspringGenerator.class);
        when(offspringGenerator.generateOffspring(emptyList)).thenReturn(emptyList);

        FastNonDominatedSort<RefactorSequence> fastNonDominatedSort = mock(FastNonDominatedSort.class);
        CrowdingDistanceSort<RefactorSequence> crowdingDistanceSort = mock(CrowdingDistanceSort.class);

        when(fastNonDominatedSort.fastNonDominatedSort(emptyList)).thenReturn(List.of());
        NSGAII<RefactorSequence> nsgaii = new NSGAII<>(populationGenerator, offspringGenerator, fastNonDominatedSort, crowdingDistanceSort, 0);
        assertTrue(nsgaii.evolve(emptyList).isEmpty());
    }
}
