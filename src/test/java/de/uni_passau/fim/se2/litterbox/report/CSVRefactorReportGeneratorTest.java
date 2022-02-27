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
        long programExtractionTime = 23;
        long refactoringSearchTime = 42;
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
        when(refactorSequence.getRefactoredProgram()).thenReturn(program);
        when(refactorSequence.getOriginalProgram()).thenReturn(program);

        Path tmpFile = Files.createTempFile("foo", "bar");
        String fileName = tmpFile.getFileName().toString();
        String pathName = tmpFile.getParent().toString();
        CSVRefactorReportGenerator reportGenerator = new CSVRefactorReportGenerator(fileName, pathName, fitnessFunctions);
        reportGenerator.generateReport(0, program, refactorSequence, populationSize, maxGen, hyperVolume, iterations, programExtractionTime, refactoringSearchTime);
        reportGenerator.close();

        List<String> lines = Files.readAllLines(tmpFile);
        Files.delete(tmpFile);

        assertThat(lines).hasSize(2);
        assertThat(lines.get(0)).contains(
                "project,pareto_index,population_size,max_generations,executed_generations,seed,hypervolume,"
                        + "program_extraction_time,refactoring_search_time");
        assertThat(lines.get(0)).contains("halstead_difficulty_fitness,number_of_blocks_fitness,category_entropy_fitness");
        assertThat(lines.get(1)).contains("helloBlockHelloBlockWithinControl,0,10,10,9,132,2.0,23,42");
        assertThat(lines.get(1)).contains("2.11,3.11,4.11");
    }

    @Test
    public void testSingleIssueTwoProjectsAppendCSV() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/refactoring/helloBlockHelloBlockWithinControl.json");
        int populationSize = 10;
        int maxGen = 10;
        int iterations = 9;
        double hyperVolume = 2.0;
        long programExtractionTime = 23;
        long refactoringSearchTime = 42;
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
        when(refactorSequence.getRefactoredProgram()).thenReturn(program);
        when(refactorSequence.getOriginalProgram()).thenReturn(program);

        Path tmpFile = Files.createTempFile("foo", "bar");
        String fileName = tmpFile.getFileName().toString();
        String pathName = tmpFile.getParent().toString();

        CSVRefactorReportGenerator reportGenerator = new CSVRefactorReportGenerator(fileName, pathName, fitnessFunctions);
        reportGenerator.generateReport(0, program, refactorSequence, populationSize, maxGen, hyperVolume, iterations, programExtractionTime, refactoringSearchTime);
        reportGenerator.close();

        CSVRefactorReportGenerator reportGenerator2 = new CSVRefactorReportGenerator(fileName, pathName, fitnessFunctions);
        reportGenerator2.generateReport(1, program, refactorSequence, populationSize, maxGen, hyperVolume, iterations, programExtractionTime, refactoringSearchTime);
        reportGenerator2.close();

        List<String> lines = Files.readAllLines(tmpFile);
        Files.delete(tmpFile);

        assertThat(lines).hasSize(3);
        assertThat(lines.get(0)).contains(
                "project,pareto_index,population_size,max_generations,executed_generations,seed,hypervolume,"
                        + "program_extraction_time,refactoring_search_time");
        assertThat(lines.get(0)).contains("halstead_difficulty_fitness,number_of_blocks_fitness,category_entropy_fitness");
        assertThat(lines.get(1)).contains("helloBlockHelloBlockWithinControl,0,10,10,9,132,2.0,23,42");
        assertThat(lines.get(1)).contains("2.11,3.11,4.11");
        assertThat(lines.get(2)).contains("helloBlockHelloBlockWithinControl,1,10,10,9,132,2.0,23,42");
        assertThat(lines.get(2)).contains("2.11,3.11,4.11");
    }
}
