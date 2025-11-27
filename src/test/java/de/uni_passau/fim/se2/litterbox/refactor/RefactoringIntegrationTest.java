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
import de.uni_passau.fim.se2.litterbox.analytics.metric.BlockCount;
import de.uni_passau.fim.se2.litterbox.analytics.metric.InterproceduralCyclomaticComplexity;
import de.uni_passau.fim.se2.litterbox.analytics.metric.ScriptCount;
import de.uni_passau.fim.se2.litterbox.analytics.refactorings.*;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.algorithms.CrowdingDistanceSort;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.algorithms.FastNonDominatedSort;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.algorithms.NSGAII;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.ChromosomeGenerator;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.FixedSizePopulationGenerator;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.OffspringGenerator;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.RefactorSequence;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.RefactorSequenceGenerator;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions.CyclomaticComplexityFitness;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions.FitnessFunction;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions.NumberOfBlocksFitness;
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

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class RefactoringIntegrationTest implements JsonTest {

    private static final int POPULATION_SIZE = 50;

    @Test
    void testIfsToConjunction() throws IOException, ParsingException {
        String code = """
                when green flag clicked
                if <key [space v] pressed?> then
                    if <mouse down?> then
                        say [Hello!]
                    end
                end
                """;
        
        Program program = parseProgram(code);
        List<RefactoringFinder> finders = List.of(new IfsToConjunctionFinder());
        
        List<RefactorSequence> solutions = runRefactoring(program, finders);
        
        verifyRefactoring(solutions, program);
    }

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
        
        verifyRefactoring(solutions, program);
    }

    @Test
    void testConjunctionToIfs() throws IOException, ParsingException {
        String code = """
                when green flag clicked
                if <<key [space v] pressed?> and <mouse down?>> then
                    say [Hello!]
                end
                """;
        
        Program program = parseProgram(code);
        List<RefactoringFinder> finders = List.of(new ConjunctionToIfsFinder());
        
        List<RefactorSequence> solutions = runRefactoring(program, finders);
        
        verifyRefactoring(solutions, program);
    }

    @Test
    void testConjunctionToIfElse() throws IOException, ParsingException {
        String code = """
                when green flag clicked
                if <<key [space v] pressed?> and <mouse down?>> then
                    say [Hello!]
                end
                if <key [space v] pressed?> then
                    say [Bye!]
                end
                """;
        
        Program program = parseProgram(code);
        List<RefactoringFinder> finders = List.of(new ConjunctionToIfElseFinder());
        
        List<RefactorSequence> solutions = runRefactoring(program, finders);
        
        verifyRefactoring(solutions, program);
    }

    @Test
    void testDisjunctionToIfElse() throws IOException, ParsingException {
        String code = """
                when green flag clicked
                if <<key [space v] pressed?> or <mouse down?>> then
                    say [Hello!]
                end
                """;
        
        Program program = parseProgram(code);
        List<RefactoringFinder> finders = List.of(new DisjunctionToIfElseFinder());
        
        List<RefactorSequence> solutions = runRefactoring(program, finders);
        
        verifyRefactoring(solutions, program);
    }

    @Test
    void testExtractLoopCondition() throws IOException, ParsingException {
        String code = """
                when green flag clicked
                forever
                    if <key [space v] pressed?> then
                        stop [this script v]
                    end
                    move (10) steps
                end
                """;
        
        Program program = parseProgram(code);
        List<RefactoringFinder> finders = List.of(new ExtractLoopConditionFinder());
        
        List<RefactorSequence> solutions = runRefactoring(program, finders);
        
        verifyRefactoring(solutions, program);
    }

    @Test
    void testExtractEventsFromForever() throws IOException, ParsingException {
        String code = """
                when green flag clicked
                forever
                    if <key [space v] pressed?> then
                        change [var1 v] by (1)
                    end
                end
                """;
        
        Program program = parseProgram(code);
        List<RefactoringFinder> finders = List.of(new ExtractEventsFromForeverFinder());
        
        List<RefactorSequence> solutions = runRefactoring(program, finders);
        
        verifyRefactoring(solutions, program);
    }

    @Test
    void testForeverIfToWaitUntil() throws IOException, ParsingException {
        String code = """
                when green flag clicked
                forever
                    if <key [space v] pressed?> then
                        say [Hello!]
                    end
                end
                """;
        
        Program program = parseProgram(code);
        List<RefactoringFinder> finders = List.of(new ForeverIfToWaitUntilFinder());
        
        List<RefactorSequence> solutions = runRefactoring(program, finders);
        
        verifyRefactoring(solutions, program);
    }

    @Test
    void testForeverWaitToForeverIf() throws IOException, ParsingException {
        String code = """
                when green flag clicked
                forever
                    wait until <key [space v] pressed?>
                    say [Hello!]
                end
                """;
        
        Program program = parseProgram(code);
        List<RefactoringFinder> finders = List.of(new ForeverWaitToForeverIfFinder());
        
        List<RefactorSequence> solutions = runRefactoring(program, finders);
        
        verifyRefactoring(solutions, program);
    }

    @Test
    void testIfIfElseToConjunction() throws IOException, ParsingException {
        String code = """
                when green flag clicked
                if <key [space v] pressed?> then
                    if <mouse down?> then
                        change [var1 v] by (1)
                    else
                        change [var2 v] by (1)
                    end
                end
                """;
        
        Program program = parseProgram(code);
        List<RefactoringFinder> finders = List.of(new IfIfElseToConjunctionFinder());
        
        List<RefactorSequence> solutions = runRefactoring(program, finders);
        
        verifyRefactoring(solutions, program);
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
        
        verifyRefactoring(solutions, program);
    }

    @Test
    void testIfElseToIfIfNot() throws IOException, ParsingException {
        String code = """
                when green flag clicked
                if <key [space v] pressed?> then
                    say [Hello!]
                else
                    say [Bye!]
                end
                """;
        
        Program program = parseProgram(code);
        List<RefactoringFinder> finders = List.of(new IfElseToIfIfNotFinder());
        
        List<RefactorSequence> solutions = runRefactoring(program, finders);
        
        verifyRefactoring(solutions, program);
    }

    @Test
    void testInlineLoopCondition() throws IOException, ParsingException {
        String code = """
                when green flag clicked
                repeat until <key [space v] pressed?>
                    move (10) steps
                end
                """;
        
        Program program = parseProgram(code);
        List<RefactoringFinder> finders = List.of(new InlineLoopConditionFinder());
        
        List<RefactorSequence> solutions = runRefactoring(program, finders);
        
        verifyRefactoring(solutions, program);
    }

    @Test
    void testLoopUnrolling() throws IOException, ParsingException {
        String code = """
                when green flag clicked
                repeat (2)
                    move (10) steps
                end
                """;
        
        Program program = parseProgram(code);
        List<RefactoringFinder> finders = List.of(new LoopUnrollingFinder());
        
        List<RefactorSequence> solutions = runRefactoring(program, finders);
        
        verifyRefactoring(solutions, program);
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
        
        verifyRefactoring(solutions, program);
    }

    @Test
    void testMergeEventsIntoForever() throws IOException, ParsingException {
        String code = """
                when [space v] key pressed
                say [Hello!]
                """;
        
        Program program = parseProgram(code);
        List<RefactoringFinder> finders = List.of(new MergeEventsIntoForeverFinder());
        
        List<RefactorSequence> solutions = runRefactoring(program, finders);
        
        verifyRefactoring(solutions, program);
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
        
        verifyRefactoring(solutions, program);
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
        
        verifyRefactoring(solutions, program);
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
        
        verifyRefactoring(solutions, program);
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
        
        verifyRefactoring(solutions, program);
    }

    @Test
    void testSplitIf() throws IOException, ParsingException {
        String code = """
                when green flag clicked
                if <key [space v] pressed?> then
                    move (10) steps
                    turn cw (15) degrees
                end
                """;
        
        Program program = parseProgram(code);
        List<RefactoringFinder> finders = List.of(new SplitIfFinder());
        
        List<RefactorSequence> solutions = runRefactoring(program, finders);
        
        verifyRefactoring(solutions, program);
    }

    @Test
    void testSplitLoop() throws IOException, ParsingException {
        String code = """
                when green flag clicked
                repeat (10)
                    move (10) steps
                    turn cw (15) degrees
                end
                """;
        
        Program program = parseProgram(code);
        List<RefactoringFinder> finders = List.of(new SplitLoopFinder());
        
        List<RefactorSequence> solutions = runRefactoring(program, finders);
        
        verifyRefactoring(solutions, program);
    }

    @Test
    void testSplitScript() throws IOException, ParsingException {
        String code = """
                when green flag clicked
                move (10) steps
                turn cw (15) degrees
                say [Hello!]
                """;
        
        Program program = parseProgram(code);
        List<RefactoringFinder> finders = List.of(new SplitScriptFinder());
        
        List<RefactorSequence> solutions = runRefactoring(program, finders);
        
        verifyRefactoring(solutions, program);
    }

    @Test
    void testSplitSlice() throws IOException, ParsingException {
        String code = """
                when green flag clicked
                set [var1 v] to (1)
                set [var2 v] to (2)
                change [var1 v] by (1)
                change [var2 v] by (1)
                """;
        
        Program program = parseProgram(code);
        List<RefactoringFinder> finders = List.of(new SplitSliceFinder());
        
        List<RefactorSequence> solutions = runRefactoring(program, finders);
        
        verifyRefactoring(solutions, program);
    }

    @Test
    void testSwapStatements() throws IOException, ParsingException {
        String code = """
                when green flag clicked
                move (10) steps
                turn cw (15) degrees
                say [Hello!]
                """;
        
        Program program = parseProgram(code);
        List<RefactoringFinder> finders = List.of(new SwapStatementsFinder());
        
        List<RefactorSequence> solutions = runRefactoring(program, finders);
        
        verifyRefactoring(solutions, program);
    }

    @Test
    void testSplitScriptAfterUntil() throws IOException, ParsingException {
        String code = """
                when green flag clicked
                repeat until <key [space v] pressed?>
                    move (10) steps
                end
                say [Hello!]
                """;
        
        Program program = parseProgram(code);
        List<RefactoringFinder> finders = List.of(new SplitScriptAfterUntilFinder());
        
        List<RefactorSequence> solutions = runRefactoring(program, finders);
        
        verifyRefactoring(solutions, program);
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
        
        verifyRefactoring(solutions, program);
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
        
        verifyRefactoring(solutions, program);
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
        
        verifyRefactoring(solutions, program);
    }

    private Program parseProgram(String code) throws IOException, ParsingException {
        Program baseProject = getAST("src/test/fixtures/emptyProject.json");
        ScratchBlocksParser parser = new ScratchBlocksParser();
        // "Sprite1" is usually the default actor in emptyProject.json
        return parser.extendProject(baseProject, "Sprite1", code);
    }

    private List<RefactorSequence> runRefactoring(Program program, List<RefactoringFinder> refactoringFinders) {
        List<FitnessFunction<RefactorSequence>> fitnessFunctions = new LinkedList<>();
        fitnessFunctions.add(new NumberOfBlocksFitness());
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

    private void verifyRefactoring(List<RefactorSequence> solutions, Program originalProgram) {
        assertFalse(solutions.isEmpty(), "Should find a refactoring solution");

        for (RefactorSequence solution : solutions) {
            // A RefactoringSolution must always have size > 0
            assertTrue(solution.getExecutedRefactorings().size() > 0, "Empty refactoring sequence found");

            Program refactoredProgram = solution.getRefactoredProgram();
                
            assertNotNull(refactoredProgram, "Refactored program must not be null");
            assertNotEquals(originalProgram, refactoredProgram, "Refactored program must differ from the original program");
                    
            // Check fitness improvement
            BlockCount<Program> blockCount = new BlockCount<>();
            double originalBlocks = blockCount.calculateMetric(originalProgram);
            double refactoredBlocks = blockCount.calculateMetric(refactoredProgram);
                    
            ScriptCount<Program> scriptCount = new ScriptCount<>();
            double originalScripts = scriptCount.calculateMetric(originalProgram);
            double refactoredScripts = scriptCount.calculateMetric(refactoredProgram);

            InterproceduralCyclomaticComplexity<Program> complexity = new InterproceduralCyclomaticComplexity<>();
            double originalComplexity = complexity.calculateMetric(originalProgram);
            double refactoredComplexity = complexity.calculateMetric(refactoredProgram);
            
            boolean worse = (refactoredBlocks > originalBlocks) || 
                                (refactoredScripts > originalScripts) || 
                                (refactoredComplexity > originalComplexity);

            // RefactoringCount is implicitly better because size > 0
            boolean improved = (refactoredBlocks < originalBlocks) || 
                                (refactoredScripts < originalScripts) || 
                                (refactoredComplexity < originalComplexity);
                    
            assertTrue(improved && !worse, 
                        "Refactored program should improve in at least one fitness dimension. " +
                        "Blocks: " + originalBlocks + " -> " + refactoredBlocks + ", " +
                        "Scripts: " + originalScripts + " -> " + refactoredScripts + ", " +
                        "Complexity: " + originalComplexity + " -> " + refactoredComplexity);
                    
            
        }        
    }
}
