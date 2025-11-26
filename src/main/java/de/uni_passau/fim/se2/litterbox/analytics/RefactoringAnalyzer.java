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
package de.uni_passau.fim.se2.litterbox.analytics;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.refactor.RefactoringTool;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.algorithms.NSGAII;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.algorithms.CrowdingDistanceSort;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.algorithms.Dominance;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.algorithms.FastNonDominatedSort;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators.BinaryRankTournament;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.ChromosomeGenerator;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.FixedSizePopulationGenerator;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.OffspringGenerator;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.RefactorSequence;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions.CohesionFitness;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions.CyclomaticComplexityFitness;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions.FitnessFunction;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions.NumberOfScriptsFitness;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators.Crossover;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators.Mutation;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators.RefactorSequenceCrossover;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators.RefactorSequenceMutation;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Logger;

public class RefactoringAnalyzer extends FileAnalyzer<Void> {

    private static final Logger log = Logger.getLogger(RefactoringAnalyzer.class.getName());
    private final List<RefactoringFinder> refactoringFinders;
    private final Path refactoredPath;

    private static final int MAX_GEN = 100; // Default value, as PropertyLoader is removed
    private static final int POPULATION_SIZE = 100; // Default value, as PropertyLoader is removed

    public RefactoringAnalyzer(Path output, Path refactoredPath, boolean delete) {
        super(new RefactoringProgramAnalyzer(), output, delete);
        this.refactoredPath = refactoredPath;
        refactoringFinders = RefactoringTool.getRefactoringFinders();
    }

    public static <T> Predicate<T> distinctByFitness(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    @Override
    protected void writeResultToFile(Path projectFile, Program program, Void result) throws IOException {
        NSGAII<RefactorSequence> nsgaii = initializeNSGAII(program, refactoringFinders);

        List<RefactorSequence> solutions = findRefactoring(nsgaii);
        if (!solutions.isEmpty()) {
            System.out.println("Found " + solutions.size() + " solutions");
            List<RefactorSequence> uniqueSolutions = solutions
                    .stream()
                    .filter(distinctByFitness(RefactorSequence::getFitnessMap))
                    .toList();
            System.out.println("Unique solutions: " + uniqueSolutions.size());

            System.out.println("Filtering for dominating solutions...");
            Dominance<RefactorSequence> dominance = new Dominance<>(nsgaii.getFitnessFunctions());
            
            // Create an empty sequence representing the original program
            RefactorSequence original = new RefactorSequence(
                    program, 
                    uniqueSolutions.get(0).getMutation(), 
                    uniqueSolutions.get(0).getCrossover(), 
                    Collections.emptyList()
            );

            // Filter solutions that dominate the original program
            List<RefactorSequence> dominatingSolutions = uniqueSolutions.stream()
                    .filter(solution -> dominance.test(solution, original))
                    .toList();

            System.out.println("Dominating solutions: " + dominatingSolutions.size());

            if (!dominatingSolutions.isEmpty()) {
                RefactoringPostProcessor postProcessor = new RefactoringPostProcessor();
                postProcessor.process(dominatingSolutions, refactoredPath, projectFile);
            } else {
                System.out.println("No solutions found that dominate the original program.");
            }
        } else {
            System.out.println("NSGA-II found no solutions!");
        }
    }




    /**
     * Execute the list of refactorings.
     *
     * @param nsgaii initialized NSGA-II
     * @return A copy of the original program with the best sequence of refactorings found applied on it.
     */
    private List<RefactorSequence> findRefactoring(NSGAII<RefactorSequence> nsgaii) {
        List<RefactorSequence> solution = nsgaii.findSolution();
        if (solution.isEmpty()) {
            return List.of();
        }

        return solution;
    }

    private NSGAII<RefactorSequence> initializeNSGAII(Program program, List<RefactoringFinder> refactoringFinders) {
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

        List<FitnessFunction<RefactorSequence>> fitnessFunctions = new LinkedList<>();
        fitnessFunctions.add(new CohesionFitness());
        fitnessFunctions.add(new CyclomaticComplexityFitness());
        fitnessFunctions.add(new NumberOfScriptsFitness());
        FastNonDominatedSort<RefactorSequence> fastNonDominatedSort = new FastNonDominatedSort<>(fitnessFunctions);
        CrowdingDistanceSort<RefactorSequence> crowdingDistanceSort = new CrowdingDistanceSort<>(fitnessFunctions);

        return new NSGAII<>(populationGenerator, offspringGenerator, fastNonDominatedSort, crowdingDistanceSort);
    }



    private static class RefactoringProgramAnalyzer implements ProgramAnalyzer<Void> {

        @Override
        public Void analyze(Program program) {
            return null;
        }
    }
}
