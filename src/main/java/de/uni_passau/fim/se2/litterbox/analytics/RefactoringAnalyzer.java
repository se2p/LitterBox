package de.uni_passau.fim.se2.litterbox.analytics;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.jsoncreation.JSONFileCreator;
import de.uni_passau.fim.se2.litterbox.refactor.RefactoringTool;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.algorithms.CrowdingDistanceSort;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.algorithms.FastNonDominatedSort;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.algorithms.NSGAII;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.*;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions.CategoryEntropyFitness;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions.FitnessFunction;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions.HalsteadDifficultyFitness;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions.NumberOfBlocksFitness;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators.*;
import de.uni_passau.fim.se2.litterbox.report.CSVRefactorReportGenerator;
import de.uni_passau.fim.se2.litterbox.report.ConsoleRefactorReportGenerator;
import de.uni_passau.fim.se2.litterbox.utils.PropertyLoader;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

public class RefactoringAnalyzer extends Analyzer {

    private static final Logger log = Logger.getLogger(RefactoringAnalyzer.class.getName());
    private final List<RefactoringFinder> refactoringFinders;
    private final String refactoredPath;

    private static final int MAX_GEN = PropertyLoader.getSystemIntProperty("nsga-ii.generations");
    private static final int POPULATION_SIZE = PropertyLoader.getSystemIntProperty("nsga-ii.populationSize");

    public RefactoringAnalyzer(String input, String output, String refactoredPath, boolean delete) {
        super(input, output, delete);
        this.refactoredPath = refactoredPath;
        refactoringFinders = RefactoringTool.getRefactoringFinders();
    }

    @Override
    void check(File fileEntry, String reportName) {
        Program program = extractProgram(fileEntry);
        if (program == null) {
            // Todo error message
            return;
        }

        List<RefactorSequence> solutions = findRefactoring(program.deepCopy(), refactoringFinders);
        if (!solutions.isEmpty()) {
            generateProjectsFromParetoFront(fileEntry, reportName, solutions);
        } else {
            System.out.println("NSGA-II found no solutions!");
        }
    }

    private void generateProjectsFromParetoFront(File fileEntry, String reportName, List<RefactorSequence> solutions) {
        for (int i = 0; i < solutions.size(); i++) {
            Program refactored = solutions.get(i).getRefactoredProgram();
            generateOutput(refactored, solutions.get(i), reportName);
            createNewProjectFileWithCounterPostfix(fileEntry, refactored, i);
        }
    }

    /**
     * Initialize NSGA-II and call it to find a sequence of refactorings.
     * Execute the list of refactorings
     *
     * @param program            The original flawed program to be refactored.
     * @param refactoringFinders All refactoring finders considered.
     * @return A copy of the original program with the best sequence of refactorings found applied on it.
     */
    private List<RefactorSequence> findRefactoring(Program program, List<RefactoringFinder> refactoringFinders) {
        NSGAII<RefactorSequence> nsgaii = initializeNSGAII(program, refactoringFinders);
        List<RefactorSequence> solution = nsgaii.findSolution();
        if (solution.isEmpty()) {
            return List.of();
        }

        return solution;
    }

    private NSGAII<RefactorSequence> initializeNSGAII(Program program, List<RefactoringFinder> refactoringFinders) {

        Crossover<RefactorSequence> crossover = new RefactorSequenceCrossover();
        Mutation<RefactorSequence> mutation = new RefactorSequenceMutation(refactoringFinders);

        ChromosomeGenerator<RefactorSequence> chromosomeGenerator = new RefactorSequenceGenerator(program.deepCopy(), mutation, crossover, refactoringFinders);
        FixedSizePopulationGenerator<RefactorSequence> populationGenerator = new FixedSizePopulationGenerator<>(chromosomeGenerator, POPULATION_SIZE);
        BinaryRankTournament<RefactorSequence> binaryRankTournament = new BinaryRankTournament<>();
        OffspringGenerator<RefactorSequence> offspringGenerator = new OffspringGenerator<>(binaryRankTournament);

        List<FitnessFunction<RefactorSequence>> fitnessFunctions = new LinkedList<>();
        fitnessFunctions.add(new HalsteadDifficultyFitness(program));
        fitnessFunctions.add(new NumberOfBlocksFitness(program));
        fitnessFunctions.add(new CategoryEntropyFitness(program));
        FastNonDominatedSort<RefactorSequence> fastNonDominatedSort = new FastNonDominatedSort<>(fitnessFunctions);
        CrowdingDistanceSort<RefactorSequence> crowdingDistanceSort = new CrowdingDistanceSort<>(fitnessFunctions);

        return new NSGAII<>(populationGenerator, offspringGenerator, fastNonDominatedSort, crowdingDistanceSort);
    }

    private void generateOutput(Program program, RefactorSequence refactorSequence, String reportFileName) {
        try {
            if (reportFileName == null || reportFileName.isEmpty()) {
                ConsoleRefactorReportGenerator reportGenerator = new ConsoleRefactorReportGenerator();
                reportGenerator.generateReport(program, refactorSequence.getExecutedRefactorings());
            } else if (FilenameUtils.getExtension(reportFileName).equals("csv")) {
                CSVRefactorReportGenerator reportGenerator = new CSVRefactorReportGenerator(reportFileName, refactoredPath, refactorSequence.getFitnessMap().keySet());
                reportGenerator.generateReport(program, refactorSequence, POPULATION_SIZE, MAX_GEN);
                reportGenerator.close();
            } else {
                throw new IllegalArgumentException("Unknown file type: " + reportFileName);
            }
        } catch (IOException e) {
            log.warning(e.getMessage());
        }
    }

    private void createNewProjectFileWithCounterPostfix(File fileEntry, Program program, int counterPostfix) {
        String outputPath = refactoredPath == null ? fileEntry.getParent() : refactoredPath;
        try {
            if ((FilenameUtils.getExtension(fileEntry.getPath())).equalsIgnoreCase("json")) {
                JSONFileCreator.writeJsonFromProgram(program, outputPath, "_refactored_" + counterPostfix);
            } else {
                JSONFileCreator.writeSb3FromProgram(program, outputPath, fileEntry, "_refactored_" + counterPostfix);
            }
        } catch (IOException e) {
            log.warning(e.getMessage());
        }
    }
}
