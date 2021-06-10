package de.uni_passau.fim.se2.litterbox.report;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.RefactorSequence;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions.CategoryEntropyFitness;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions.FitnessFunction;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions.HalsteadDifficultyFitness;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions.NumberOfBlocksFitness;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.*;
import de.uni_passau.fim.se2.litterbox.utils.Randomness;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CSVRefactorReportGeneratorTest implements JsonTest {

    @Test
    public void testSingleRefactoringSingleProjectNewCSV() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/refactoring/helloBlockHelloBlockWithinControl.json");
        int populationSize = 10;
        int maxGen = 10;
        int iterations = 9;
        double hyperVolume = 2.0;
        Randomness.setSeed(132);
        FitnessFunction<RefactorSequence> f1 = new HalsteadDifficultyFitness();
        FitnessFunction<RefactorSequence> f2 = new NumberOfBlocksFitness();
        FitnessFunction<RefactorSequence> f3 = new CategoryEntropyFitness();
        Set<FitnessFunction<RefactorSequence>> fitnessFunctions = new LinkedHashSet<>();
        fitnessFunctions.add(f1);
        fitnessFunctions.add(f2);
        fitnessFunctions.add(f3);

        RefactorSequence refactorSequence = mock(RefactorSequence.class);

        Map<FitnessFunction<RefactorSequence>, Double> fitnessMap = new LinkedHashMap<>();
        fitnessMap.put(f1, 2.11);
        fitnessMap.put(f2, 3.11);
        fitnessMap.put(f3, 4.11);

        Refactoring r1 = mock(SplitScript.class);
        Refactoring r2 = mock(SplitScript.class);
        Refactoring r3 = mock(MergeDoubleIf.class);

        when(r1.getName()).thenReturn("split_script");
        when(r2.getName()).thenReturn("split_script");
        when(r3.getName()).thenReturn("merge_double_if");

        when(refactorSequence.getExecutedRefactorings()).thenReturn(List.of(r1, r2, r3));
        when(refactorSequence.getFitnessMap()).thenReturn((fitnessMap));

        Path tmpFile = Files.createTempFile("foo", "bar");
        String fileName = tmpFile.getFileName().toString();
        String pathName = tmpFile.getParent().toString();
        CSVRefactorReportGenerator reportGenerator = new CSVRefactorReportGenerator(fileName, pathName, fitnessFunctions);
        reportGenerator.generateReport(program, refactorSequence, populationSize, maxGen, hyperVolume, iterations);
        reportGenerator.close();

        List<String> lines = Files.readAllLines(tmpFile);
        tmpFile.toFile().delete();

        assertThat(lines).hasSize(2);
        assertThat(lines.get(0)).contains("project,population_size,max_generations,executed_generations,seed,hypervolume");
        assertThat(lines.get(0)).contains("halstead_difficulty_fitness,number_of_blocks_fitness,category_entropy_fitness");
        assertThat(lines.get(1)).contains("helloBlockHelloBlockWithinControl,10,10,9,132,2.0");
        assertThat(lines.get(1)).contains("2.11,3.11,4.11");
    }

    @Test
    public void testSingleIssueTwoProjectsAppendCSV() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/refactoring/helloBlockHelloBlockWithinControl.json");
        int populationSize = 10;
        int maxGen = 10;
        int iterations = 9;
        double hyperVolume = 2.0;
        Randomness.setSeed(132);
        FitnessFunction<RefactorSequence> f1 = new HalsteadDifficultyFitness();
        FitnessFunction<RefactorSequence> f2 = new NumberOfBlocksFitness();
        FitnessFunction<RefactorSequence> f3 = new CategoryEntropyFitness();
        Set<FitnessFunction<RefactorSequence>> fitnessFunctions = new LinkedHashSet<>();
        fitnessFunctions.add(f1);
        fitnessFunctions.add(f2);
        fitnessFunctions.add(f3);

        RefactorSequence refactorSequence = mock(RefactorSequence.class);

        Map<FitnessFunction<RefactorSequence>, Double> fitnessMap = new LinkedHashMap<>();
        fitnessMap.put(f1, 2.11);
        fitnessMap.put(f2, 3.11);
        fitnessMap.put(f3, 4.11);

        Refactoring r1 = mock(SplitScript.class);
        Refactoring r2 = mock(SplitScript.class);
        Refactoring r3 = mock(MergeDoubleIf.class);

        when(r1.getName()).thenReturn("split_script");
        when(r2.getName()).thenReturn("split_script");
        when(r3.getName()).thenReturn("merge_double_if");

        when(refactorSequence.getExecutedRefactorings()).thenReturn(List.of(r1, r2, r3));
        when(refactorSequence.getFitnessMap()).thenReturn((fitnessMap));

        Path tmpFile = Files.createTempFile("foo", "bar");
        String fileName = tmpFile.getFileName().toString();
        String pathName = tmpFile.getParent().toString();

        CSVRefactorReportGenerator reportGenerator = new CSVRefactorReportGenerator(fileName, pathName, fitnessFunctions);
        reportGenerator.generateReport(program, refactorSequence, populationSize, maxGen, hyperVolume, iterations);
        reportGenerator.close();

        CSVRefactorReportGenerator reportGenerator2 = new CSVRefactorReportGenerator(fileName, pathName, fitnessFunctions);
        reportGenerator2.generateReport(program, refactorSequence, populationSize, maxGen, hyperVolume, iterations);
        reportGenerator2.close();

        List<String> lines = Files.readAllLines(tmpFile);
        tmpFile.toFile().delete();

        assertThat(lines).hasSize(3);
        assertThat(lines.get(0)).contains("project,population_size,max_generations,executed_generations,seed,hypervolume");
        assertThat(lines.get(0)).contains("halstead_difficulty_fitness,number_of_blocks_fitness,category_entropy_fitness");
        assertThat(lines.get(1)).contains("helloBlockHelloBlockWithinControl,10,10,9,132,2.0");
        assertThat(lines.get(1)).contains("2.11,3.11,4.11");
        assertThat(lines.get(2)).contains("helloBlockHelloBlockWithinControl,10,10,9,132,2.0");
        assertThat(lines.get(2)).contains("2.11,3.11,4.11");
    }
}
