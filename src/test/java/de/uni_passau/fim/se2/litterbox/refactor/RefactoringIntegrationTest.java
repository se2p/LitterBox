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
package de.uni_passau.fim.se2.litterbox.refactor;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.RefactoringFinder;
import de.uni_passau.fim.se2.litterbox.analytics.refactorings.*;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.util.StructuralEquality;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.algorithms.CrowdingDistanceSort;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.algorithms.FastNonDominatedSort;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.algorithms.NSGAII;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.ChromosomeGenerator;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.FixedSizePopulationGenerator;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.OffspringGenerator;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.RefactorSequence;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.RefactorSequenceGenerator;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions.CohesionFitness;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions.CyclomaticComplexityFitness;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions.FitnessFunction;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions.NumberOfScriptsFitness;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators.BinaryRankTournament;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators.Crossover;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators.Mutation;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators.RefactorSequenceCrossover;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators.RefactorSequenceMutation;
import de.uni_passau.fim.se2.litterbox.scratchblocks.ScratchBlocksParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class RefactoringIntegrationTest implements JsonTest {

    private static final int POPULATION_SIZE = 50;

    @Test
    void testSequenceToLoop() throws IOException, ParsingException {
        String code = """
                when green flag clicked
                move (10) steps
                turn cw (15) degrees
                move (10) steps
                turn cw (15) degrees
                move (10) steps
                turn cw (15) degrees
                """;
        
        Program program = parseProgram(code);
        List<RefactoringFinder> finders = List.of(new SequenceToLoopFinder());        
        List<RefactorSequence> solutions = runRefactoring(program, finders);
        
        assertEquals(1, solutions.size());
        String expectedCode = """
                when green flag clicked
                repeat (3)
                    move (10) steps
                    turn right (15) degrees
                end
                """;
        
        Program expectedProgram = parseProgram(expectedCode);
        assertTrue(StructuralEquality.areStructurallyEqual(expectedProgram, solutions.getFirst().buildRefactoredProgram()));
    }

    @Test
    void testConjunctionToIfElse() throws IOException, ParsingException {
        String code = """
                when green flag clicked
                if <<key (space v) pressed?> and <mouse down?>> then
                    say [Hello!]
                end
                if <key (space v) pressed?> then
                    say [Bye!]
                end
                """;
        
        Program program = parseProgram(code);
        List<RefactoringFinder> finders = List.of(new ConjunctionToIfElseFinder());
        List<RefactorSequence> solutions = runRefactoring(program, finders);

        assertEquals(1, solutions.size());
        String expectedCode = """
                when green flag clicked
                if <key (space v) pressed?> then
                    if  <mouse down?> then
                        say [Hello!]
                    else
                        say [Bye!]
                    end
                end
                """;
        
        Program expectedProgram = parseProgram(expectedCode);
        Program refactoredProgram = solutions.getFirst().buildRefactoredProgram();
        assertTrue(StructuralEquality.areStructurallyEqual(expectedProgram, refactoredProgram));
    }

    @Test
    void testExtractLoopCondition() throws IOException, ParsingException {
        String code = """
                when green flag clicked
                forever
                    if <key (space v) pressed?> then
                        stop [this script v]
                    end
                    move (10) steps
                end
                """;
        
        Program program = parseProgram(code);
        List<RefactoringFinder> finders = List.of(new ExtractLoopConditionFinder());
        List<RefactorSequence> solutions = runRefactoring(program, finders);
        
        assertEquals(1, solutions.size());
        String expectedCode = """
                when green flag clicked
                repeat until <key (space v) pressed?>
                    move (10) steps
                end
                """;
        
        Program expectedProgram = parseProgram(expectedCode);
        Program refactoredProgram = solutions.getFirst().buildRefactoredProgram();
        assertTrue(StructuralEquality.areStructurallyEqual(expectedProgram, refactoredProgram));
    }

    @Test
    void testExtractEventsFromForever() throws IOException, ParsingException {
        String code = """
                when green flag clicked
                forever
                    if <key (space v) pressed?> then
                        change [var1 v] by (1)
                    end
                end
                """;
        
        Program program = parseProgram(code);
        List<RefactoringFinder> finders = List.of(new ExtractEventsFromForeverFinder());
        List<RefactorSequence> solutions = runRefactoring(program, finders);

        assertEquals(1, solutions.size());
        String expectedCode = """
                when [space v] key pressed
                change [var1 v] by (1)
                """;
        
        Program expectedProgram = parseProgram(expectedCode);
        Program refactoredProgram = solutions.getFirst().buildRefactoredProgram();
        assertTrue(StructuralEquality.areStructurallyEqual(expectedProgram, refactoredProgram));
    }

    @Test
    void testIfElseToDisjunction() throws IOException, ParsingException {
        String code = """
                when green flag clicked
                if <key [space v] pressed?> then
                    say [Hello!]
                else
                    if <mouse down?> then
                        say [Hello!]
                    end
                end
                """;
        
        Program program = parseProgram(code);
        List<RefactoringFinder> finders = List.of(new IfElseToDisjunctionFinder());
        List<RefactorSequence> solutions = runRefactoring(program, finders);

        assertEquals(1, solutions.size());
        String expectedCode = """
                when green flag clicked
                if <<key [space v] pressed?> or <mouse down?>> then
                    say [Hello!]
                end
                """;
        
        Program expectedProgram = parseProgram(expectedCode);
        Program refactoredProgram = solutions.getFirst().buildRefactoredProgram();
        assertTrue(StructuralEquality.areStructurallyEqual(expectedProgram, refactoredProgram));
     }

    @Test
    void testMergeDoubleIf() throws IOException, ParsingException {
        String code = """
                when green flag clicked
                if <key [space v] pressed?> then
                    move (10) steps
                end
                if <key [space v] pressed?> then
                    turn cw (15) degrees
                end
                """;
        
        Program program = parseProgram(code);
        List<RefactoringFinder> finders = List.of(new MergeDoubleIfFinder());
        List<RefactorSequence> solutions = runRefactoring(program, finders);

        assertEquals(1, solutions.size());
        String expectedCode = """
                when green flag clicked
                if <key [space v] pressed?> then
                    move (10) steps
                    turn cw (15) degrees
                end
                """;
        
        Program expectedProgram = parseProgram(expectedCode);
        Program refactoredProgram = solutions.getFirst().buildRefactoredProgram();
        assertTrue(StructuralEquality.areStructurallyEqual(expectedProgram, refactoredProgram));
    }

    @Test
    void testMergeLoops() throws IOException, ParsingException {
        String code = """
                when green flag clicked
                repeat (10)
                    change [var1 v] by (1)
                end
                
                when green flag clicked
                repeat (10)
                    change [var2 v] by (1)
                end
                """;
        
        Program program = parseProgram(code);
        List<RefactoringFinder> finders = List.of(new MergeLoopsFinder());
        List<RefactorSequence> solutions = runRefactoring(program, finders);

        String expectedCode1 = """
                when green flag clicked
                repeat (10)
                    change [var1 v] by (1)
                    change [var2 v] by (1)
                end
                """;
        String expectedCode2 = """
                when green flag clicked
                repeat (10)
                    change [var2 v] by (1)
                    change [var1 v] by (1)
                end
                """;
        
        Program expectedProgram1 = parseProgram(expectedCode1);
        Program expectedProgram2 = parseProgram(expectedCode2);
        Program refactoredProgram = solutions.getFirst().buildRefactoredProgram();
        assertTrue(StructuralEquality.areStructurallyEqual(expectedProgram1, refactoredProgram) ||
                   StructuralEquality.areStructurallyEqual(expectedProgram2, refactoredProgram));
    }

    @Test
    void testMergeScripts() throws IOException, ParsingException {
        String code = """
                when green flag clicked
                move (10) steps
                
                when green flag clicked
                turn cw (15) degrees
                """;
        
        Program program = parseProgram(code);
        List<RefactoringFinder> finders = List.of(new MergeScriptsFinder());
        List<RefactorSequence> solutions = runRefactoring(program, finders);
        
        String expectedCode1 = """
                when green flag clicked
                move (10) steps
                turn right (15) degrees
                """;

        String expectedCode2 = """
                when green flag clicked
                turn right (15) degrees
                move (10) steps
                """;

        Program expectedProgram1 = parseProgram(expectedCode1);
        Program expectedProgram2 = parseProgram(expectedCode2);
        Program refactoredProgram = solutions.getFirst().buildRefactoredProgram();
        assertTrue(StructuralEquality.areStructurallyEqual(expectedProgram1, refactoredProgram) ||
    StructuralEquality.areStructurallyEqual(expectedProgram2, refactoredProgram));
     }

    @Test
    void testIfIfNotToIfElse() throws IOException, ParsingException {
        String code = """
                when green flag clicked
                if <key [space v] pressed?> then
                    say [Hello!]
                end
                if <not <key [space v] pressed?>> then
                    say [Bye!]
                end
                """;
        
        Program program = parseProgram(code);
        List<RefactoringFinder> finders = List.of(new IfIfNotToIfElseFinder());
        List<RefactorSequence> solutions = runRefactoring(program, finders);
        

        assertEquals(1, solutions.size());
        String expectedCode = """
                when green flag clicked
                if <key [space v] pressed?> then
                    say [Hello!]
                else
                    say [Bye!]
                end
                """;
        
        Program expectedProgram = parseProgram(expectedCode);
        Program refactoredProgram = solutions.getFirst().buildRefactoredProgram();
        assertTrue(StructuralEquality.areStructurallyEqual(expectedProgram, refactoredProgram));
     }

    @Test
    void testMergeScriptsAfterUntil() throws IOException, ParsingException {
        String code = """
                when green flag clicked
                repeat until <key [space v] pressed?>
                    move (10) steps
                end
                
                when green flag clicked
                wait until <key [space v] pressed?>
                say [Done!]
                """;
        
        Program program = parseProgram(code);
        List<RefactoringFinder> finders = List.of(new MergeScriptsAfterUntilFinder());
        List<RefactorSequence> solutions = runRefactoring(program, finders);
        
        assertEquals(1, solutions.size());
        String expectedCode = """
                when green flag clicked
                repeat until <key [space v] pressed?>
                    move (10) steps
                end
                say [Done!]
                """;
        
        Program expectedProgram = parseProgram(expectedCode);
        Program refactoredProgram = solutions.getFirst().buildRefactoredProgram();
        assertTrue(StructuralEquality.areStructurallyEqual(expectedProgram, refactoredProgram));
      }

    @Test
    void testMultipleRefactoringsInSingleScript() throws IOException, ParsingException {
        String code = """
                when green flag clicked
                move (10) steps
                turn cw (15) degrees
                move (10) steps
                turn cw (15) degrees
                if <key [space v] pressed?> then
                    move (10) steps
                end
                if <key [space v] pressed?> then
                    turn cw (15) degrees
                end
                """;
        
        Program program = parseProgram(code);
        List<RefactoringFinder> finders = List.of(new SequenceToLoopFinder(), new MergeDoubleIfFinder());
        List<RefactorSequence> solutions = runRefactoring(program, finders);

        String expectedCode = """
                when green flag clicked
                repeat (2)
                    move (10) steps
                    turn right (15) degrees
                end
                if <key [space v] pressed?> then
                    move (10) steps
                    turn right (15) degrees
                end
                """;
        
        Program expectedProgram = parseProgram(expectedCode);
        assertTrue(solutions.stream().anyMatch(p -> StructuralEquality.areStructurallyEqual(expectedProgram, p.buildRefactoredProgram())));
    }

    @Test
    void testMultipleScriptsWithRefactorings() throws IOException, ParsingException {
        String code = """
                when green flag clicked
                move (10) steps
                turn cw (15) degrees
                move (10) steps
                turn cw (15) degrees
                
                when green flag clicked
                if <key [space v] pressed?> then
                    move (10) steps
                end
                if <key [space v] pressed?> then
                    turn cw (15) degrees
                end
                """;
        
        Program program = parseProgram(code);
        List<RefactoringFinder> finders = List.of(new SequenceToLoopFinder(), new MergeDoubleIfFinder());
        
        List<RefactorSequence> solutions = runRefactoring(program, finders);

        String expectedCode = """
                when green flag clicked
                repeat (2)
                    move (10) steps
                    turn right (15) degrees
                end

                when green flag clicked
                if <key [space v] pressed?> then
                    move (10) steps
                    turn right (15) degrees
                end
                """;
        
        Program expectedProgram = parseProgram(expectedCode);
        assertTrue(solutions.stream().anyMatch(p -> StructuralEquality.areStructurallyEqual(expectedProgram, p.buildRefactoredProgram())));

    }

    @Test
    void testThreeRefactoringsInSingleScript() throws IOException, ParsingException {
        String code = """
                when green flag clicked
                move (10) steps
                turn cw (15) degrees
                move (10) steps
                turn cw (15) degrees
                if <key [space v] pressed?> then
                    if <mouse down?> then
                        say [Hello!]
                    end
                end
                if <key [a v] pressed?> then
                    move (10) steps
                end
                if <key [a v] pressed?> then
                    turn cw (15) degrees
                end
                """;
        
        Program program = parseProgram(code);
        List<RefactoringFinder> finders = List.of(
            new SequenceToLoopFinder(), 
            new IfsToConjunctionFinder(),
            new MergeDoubleIfFinder()
        );
        
        List<RefactorSequence> solutions = runRefactoring(program, finders);

        String expectedCode = """
                when green flag clicked
                repeat (2)
                    move (10) steps
                    turn right (15) degrees
                end
                if <key [space v] pressed?> then
                    if <mouse down?> then
                        say [Hello!]
                    end
                end
                if <key [a v] pressed?> then
                    move (10) steps
                    turn right (15) degrees
                end
                """;
        
        Program expectedProgram = parseProgram(expectedCode);
        assertTrue(solutions.stream().anyMatch(p -> StructuralEquality.areStructurallyEqual(expectedProgram, p.buildRefactoredProgram())));

    }

    @Test
    void testParetoFrontDominance() throws IOException, ParsingException {
        String code = """
                when green flag clicked
                move (10) steps
                turn cw (15) degrees
                move (10) steps
                turn cw (15) degrees
                if <key [space v] pressed?> then
                    if <mouse down?> then
                        say [Hello!]
                    end
                end
                if <key [a v] pressed?> then
                    move (10) steps
                end
                if <key [a v] pressed?> then
                    turn cw (15) degrees
                end
                """;

        Program program = parseProgram(code);
        List<RefactoringFinder> finders = List.of(
                new SequenceToLoopFinder(),
                new IfsToConjunctionFinder(),
                new MergeDoubleIfFinder()
        );

        List<FitnessFunction<RefactorSequence>> fitnessFunctions = new LinkedList<>();
        fitnessFunctions.add(new CohesionFitness());
        fitnessFunctions.add(new NumberOfScriptsFitness());
        fitnessFunctions.add(new CyclomaticComplexityFitness());

        NSGAII<RefactorSequence> nsgaii = initializeNSGAII(program, finders, fitnessFunctions);
        List<RefactorSequence> paretoFront = nsgaii.findSolution();
        List<RefactorSequence> population = nsgaii.getFinalPopulation();

        assertFalse(paretoFront.isEmpty(), "Pareto front should not be empty");
        assertFalse(population.isEmpty(), "Population should not be empty");

        // Verify that every solution in the population that is NOT in the Pareto front
        // is dominated by at least one solution in the Pareto front.
        for (RefactorSequence other : population) {
            if (paretoFront.contains(other)) {
                continue;
            }

            boolean dominated = false;
            for (RefactorSequence pareto : paretoFront) {
                if (dominates(pareto, other, fitnessFunctions)) {
                    dominated = true;
                    break;
                }
            }
            assertTrue(dominated, "Solution " + other + " is not in Pareto front but is not dominated by any Pareto solution");
        }
    }

    private boolean dominates(RefactorSequence p1, RefactorSequence p2, List<FitnessFunction<RefactorSequence>> fitnessFunctions) {
        boolean atLeastOneBetter = false;
        for (FitnessFunction<RefactorSequence> ff : fitnessFunctions) {
            double val1 = p1.getFitness(ff);
            double val2 = p2.getFitness(ff);

            if (ff.isMinimizing()) {
                if (val1 > val2) return false; // p1 is worse than p2
                if (val1 < val2) atLeastOneBetter = true;
            } else {
                if (val1 < val2) return false; // p1 is worse than p2
                if (val1 > val2) atLeastOneBetter = true;
            }
        }
        return atLeastOneBetter;
    }


    private Program parseProgram(String code) throws IOException, ParsingException {
        Program baseProject = getAST("src/test/fixtures/emptyProject.json");
        ScratchBlocksParser parser = new ScratchBlocksParser();
        // "Sprite1" is usually the default actor in emptyProject.json
        return parser.extendProject(baseProject, "Sprite1", code);
    }

    private List<RefactorSequence> runRefactoring(Program program, List<RefactoringFinder> refactoringFinders) {
        List<FitnessFunction<RefactorSequence>> fitnessFunctions = new LinkedList<>();
        fitnessFunctions.add(new CohesionFitness());
        fitnessFunctions.add(new NumberOfScriptsFitness());
        fitnessFunctions.add(new CyclomaticComplexityFitness());
        return runRefactoring(program, refactoringFinders, fitnessFunctions);
    }

    private List<RefactorSequence> runRefactoring(Program program, List<RefactoringFinder> refactoringFinders, List<FitnessFunction<RefactorSequence>> fitnessFunctions) {
        NSGAII<RefactorSequence> nsgaii = initializeNSGAII(program, refactoringFinders, fitnessFunctions);
        return nsgaii.findSolution();
    }

    private NSGAII<RefactorSequence> initializeNSGAII(Program program, List<RefactoringFinder> refactoringFinders, List<FitnessFunction<RefactorSequence>> fitnessFunctions) {
        Crossover<RefactorSequence> crossover = new RefactorSequenceCrossover();
        Mutation<RefactorSequence> mutation = new RefactorSequenceMutation(refactoringFinders);

        ChromosomeGenerator<RefactorSequence> chromosomeGenerator = new RefactorSequenceGenerator(
                program, mutation, crossover, refactoringFinders
        );
        FixedSizePopulationGenerator<RefactorSequence> populationGenerator = new FixedSizePopulationGenerator<>(
                chromosomeGenerator, POPULATION_SIZE
        );
        BinaryRankTournament<RefactorSequence> binaryRankTournament = new BinaryRankTournament<>();
        OffspringGenerator<RefactorSequence> offspringGenerator = new OffspringGenerator<>(binaryRankTournament);

        FastNonDominatedSort<RefactorSequence> fastNonDominatedSort = new FastNonDominatedSort<>(fitnessFunctions);
        CrowdingDistanceSort<RefactorSequence> crowdingDistanceSort = new CrowdingDistanceSort<>(fitnessFunctions);

        return new NSGAII<>(populationGenerator, offspringGenerator, fastNonDominatedSort, crowdingDistanceSort);
    }
}
