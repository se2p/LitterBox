/*
 * Copyright (C) 2019-2026 LitterBox contributors
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
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.algorithms.CrowdingDistanceSort;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.algorithms.FastNonDominatedSort;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.algorithms.NSGAII;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.*;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions.CohesionFitness;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions.CyclomaticComplexityFitness;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions.FitnessFunction;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions.NumberOfScriptsFitness;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators.*;
import de.uni_passau.fim.se2.litterbox.utils.PropertyLoader;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

public class RefactoringAnalyzer extends FileAnalyzer<Void> {

    private static final Logger log = Logger.getLogger(RefactoringAnalyzer.class.getName());
    private final List<RefactoringFinder> refactoringFinders;

    public RefactoringAnalyzer(Path output, boolean delete) {
        super(new RefactoringProgramAnalyzer(), output, delete);
        refactoringFinders = RefactoringTool.getRefactoringFinders();
    }

    @Override
    protected void writeResultToFile(Path projectFile, Program program, Void result) throws IOException {
        NSGAII<RefactorSequence> nsgaii = initializeNSGAII(program, refactoringFinders);

        List<RefactorSequence> solutions = findRefactoring(nsgaii);

        // Export population to CSV
        List<List<RefactorSequence>> fronts = nsgaii.getFinalFronts();
        if (fronts != null && !fronts.isEmpty()
                && PropertyLoader.getSystemBooleanProperty("nsga-ii.write_last_population")) {
            exportPopulationToCSV(fronts, nsgaii.getFitnessFunctions());
        }

        if (!solutions.isEmpty()) {
            RefactoringPostProcessor postProcessor = new RefactoringPostProcessor(nsgaii.getFitnessFunctions());
            postProcessor.process(solutions, program, output, projectFile);
        } else {
            log.info("NSGA-II found no solutions!");
        }
    }

    private void exportPopulationToCSV(
            List<List<RefactorSequence>> fronts, List<FitnessFunction<RefactorSequence>> fitnessFunctions
    ) {
        Path csvFile = output.resolve("nsgaii_population.csv");
        try (PrintWriter writer = new PrintWriter(csvFile.toFile())) {
            // Write header
            writer.print("ID,Rank");
            for (FitnessFunction<RefactorSequence> ff : fitnessFunctions) {
                writer.print("," + ff.getName());
            }
            writer.println();

            // Write data
            int id = 1;
            for (List<RefactorSequence> front : fronts) {
                for (RefactorSequence solution : front) {
                    writer.print(id++ + "," + solution.getRank());
                    for (FitnessFunction<RefactorSequence> ff : fitnessFunctions) {
                        writer.print("," + solution.getFitness(ff));
                    }
                    writer.println();
                }
            }
        } catch (IOException e) {
            log.severe("Failed to write NSGA-II population to CSV: " + e.getMessage());
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
                chromosomeGenerator, PropertyLoader.getSystemIntProperty("nsga-ii.populationSize")
        );
        BinaryRankTournament<RefactorSequence> binaryRankTournament = new BinaryRankTournament<>();
        OffspringGenerator<RefactorSequence> offspringGenerator = new OffspringGenerator<>(binaryRankTournament);

        List<FitnessFunction<RefactorSequence>> fitnessFunctions = loadFitnessFunctions();

        FastNonDominatedSort<RefactorSequence> fastNonDominatedSort = new FastNonDominatedSort<>(fitnessFunctions);
        CrowdingDistanceSort<RefactorSequence> crowdingDistanceSort = new CrowdingDistanceSort<>(fitnessFunctions);

        return new NSGAII<>(populationGenerator, offspringGenerator, fastNonDominatedSort, crowdingDistanceSort);
    }

    @SuppressWarnings("unchecked")
    private List<FitnessFunction<RefactorSequence>> loadFitnessFunctions() {
        List<FitnessFunction<RefactorSequence>> fitnessFunctions = new LinkedList<>();
        String property = PropertyLoader.getSystemStringProperty("refactoring.fitness_functions");

        if (property == null || property.trim().isEmpty()) {
            // Default fitness functions
            fitnessFunctions.add(new CohesionFitness());
            fitnessFunctions.add(new CyclomaticComplexityFitness());
            fitnessFunctions.add(new NumberOfScriptsFitness());
            return fitnessFunctions;
        }

        String[] classNames = property.split(",");
        for (String className : classNames) {
            className = className.trim();
            if (!className.contains(".")) {
                className = "de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions." + className;
            }
            try {
                Class<?> clazz = Class.forName(className);
                if (FitnessFunction.class.isAssignableFrom(clazz)) {
                    fitnessFunctions.add(
                            (FitnessFunction<RefactorSequence>) clazz.getDeclaredConstructor().newInstance()
                    );
                } else {
                    log.warning("Class " + className + " does not implement FitnessFunction. Skipping.");
                }
            } catch (Exception e) {
                log.warning("Failed to load fitness function " + className + ": " + e.getMessage() + ". Skipping.");
            }
        }

        if (fitnessFunctions.isEmpty()) {
            log.warning("No valid fitness functions loaded from property. Using defaults.");
            fitnessFunctions.add(new CohesionFitness());
            fitnessFunctions.add(new CyclomaticComplexityFitness());
            fitnessFunctions.add(new NumberOfScriptsFitness());
        }

        return fitnessFunctions;
    }

    private static class RefactoringProgramAnalyzer implements ProgramAnalyzer<Void> {

        @Override
        public Void analyze(Program program) {
            return null;
        }
    }
}
