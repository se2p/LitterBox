package de.uni_passau.fim.se2.litterbox.analytics;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.jsoncreation.JSONFileCreator;
import de.uni_passau.fim.se2.litterbox.refactor.RefactoringTool;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.algorithms.CrowdingDistanceSort;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.algorithms.FastNonDominatedSort;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.algorithms.NSGAII;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.*;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions.*;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators.*;
import de.uni_passau.fim.se2.litterbox.report.CSVRefactorReportGenerator;
import de.uni_passau.fim.se2.litterbox.report.ConsoleRefactorReportGenerator;
import de.uni_passau.fim.se2.litterbox.utils.HyperVolume2D;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;
import de.uni_passau.fim.se2.litterbox.utils.PropertyLoader;

import java.time.Duration;
import java.time.Instant;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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
    void check(File fileEntry, String reportName) throws IOException {
        final Instant startProgramExtraction = Instant.now();
        Program program = extractProgram(fileEntry);
        if (program == null) {
            // Todo error message
            return;
        }
        final Instant endProgramExtraction = Instant.now();
        final long programExtractionTime = Duration.between(startProgramExtraction, endProgramExtraction).toMillis();

        NSGAII<RefactorSequence> nsgaii = initializeNSGAII(program, refactoringFinders);

        final Instant startRefactoringSearch = Instant.now();
        List<RefactorSequence> solutions = findRefactoring(nsgaii);
        final Instant endRefactoringSearch = Instant.now();
        final long refactoringSearchTime = Duration.between(startRefactoringSearch, endRefactoringSearch).toMillis();
        if (!solutions.isEmpty()) {
            int iteration = nsgaii.getIteration();
            generateProjectsFromParetoFront(fileEntry, reportName, nsgaii.getFitnessFunctions(), solutions, iteration,
                    programExtractionTime, refactoringSearchTime, program);
        } else {
            System.out.println("NSGA-II found no solutions!");
        }
    }

    private void generateProjectsFromParetoFront(File fileEntry, String reportName,
            List<FitnessFunction<RefactorSequence>> fitnessFunctions, List<RefactorSequence> solutions, int iteration,
            long programExtractionTime, long refactoringSearchTime, Program originalProgram) throws IOException {
        Preconditions.checkArgument(fitnessFunctions.size() >= 2);
        NormalizingFitnessFunction<RefactorSequence> ff1 = new NormalizingFitnessFunction<>(fitnessFunctions.get(0));
        NormalizingFitnessFunction<RefactorSequence> ff2 = new NormalizingFitnessFunction<>(fitnessFunctions.get(1));
        final HyperVolume2D<RefactorSequence> hv = new HyperVolume2D<>(ff1, ff2, ff1.getReferencePoint(), ff2.getReferencePoint());
        double hyperVolumeValue = hv.compute(solutions.stream().map(RefactorSequence::copy).collect(Collectors.toList()));

        List<String> fitnessValuesWithoutRefactoring = new LinkedList<>();
        RefactorSequence emptyRefactorSequence = createEmptyRefactorSequence(originalProgram);
        for (FitnessFunction<RefactorSequence> fitnessFunction : solutions.get(0).getFitnessMap().keySet()) {
            String fitnessValueWithoutRefactoring = String.valueOf(fitnessFunction.getFitness(emptyRefactorSequence));
            fitnessValuesWithoutRefactoring.add(fitnessValueWithoutRefactoring);
        }
        for (int i = 0; i < solutions.size(); i++) {
            log.log(Level.FINE, "Refactoring " + i);
            log.log(Level.FINE, "Original program");
            log.log(Level.FINE, solutions.get(i).getOriginalProgram().getScratchBlocks());
            log.log(Level.FINE, "Refactoring sequence");
            log.log(Level.FINE, solutions.get(i).getExecutedRefactorings().toString());
            log.log(Level.FINE, "Refactored program");
            log.log(Level.FINE, solutions.get(i).getRefactoredProgram().getScratchBlocks());
            Program refactored = solutions.get(i).getRefactoredProgram();
            generateOutput(
                    refactored, solutions.get(i), reportName, hyperVolumeValue, iteration, programExtractionTime,
                    refactoringSearchTime, fitnessValuesWithoutRefactoring);
            createNewProjectFileWithCounterPostfix(fileEntry, refactored, i);
        }
    }

    private RefactorSequence createEmptyRefactorSequence(Program program) {
        Crossover<RefactorSequence> crossover = new RefactorSequenceCrossover();
        Mutation<RefactorSequence> mutation = new RefactorSequenceMutation(List.of());
        return new RefactorSequence(program, mutation, crossover, List.of(), List.of());
    }

    /**
     * Execute the list of refactorings
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

        ChromosomeGenerator<RefactorSequence> chromosomeGenerator = new RefactorSequenceGenerator(program.deepCopy(), mutation, crossover, refactoringFinders);
        FixedSizePopulationGenerator<RefactorSequence> populationGenerator = new FixedSizePopulationGenerator<>(chromosomeGenerator, POPULATION_SIZE);
        BinaryRankTournament<RefactorSequence> binaryRankTournament = new BinaryRankTournament<>();
        OffspringGenerator<RefactorSequence> offspringGenerator = new OffspringGenerator<>(binaryRankTournament);

        List<FitnessFunction<RefactorSequence>> fitnessFunctions = new LinkedList<>();
        fitnessFunctions.add(new SumComplexityFitness());
        fitnessFunctions.add(new SumEntropyFitness());
        //fitnessFunctions.add(new AverageComplexityFitness());
        //fitnessFunctions.add(new AverageEntropyFitness());

        // fitnessFunctions.add(new HalsteadDifficultyFitness());
        // fitnessFunctions.add(new NumberOfBlocksFitness(program));
        // fitnessFunctions.add(new CategoryEntropyFitness());
        FastNonDominatedSort<RefactorSequence> fastNonDominatedSort = new FastNonDominatedSort<>(fitnessFunctions);
        CrowdingDistanceSort<RefactorSequence> crowdingDistanceSort = new CrowdingDistanceSort<>(fitnessFunctions);

        return new NSGAII<>(populationGenerator, offspringGenerator, fastNonDominatedSort, crowdingDistanceSort);
    }

    private void generateOutput(Program program, RefactorSequence refactorSequence, String reportFileName, double hyperVolume, int iteration, long programExtractionTime, long refactoringSearchTime, List<String> fitnessValuesWithoutRefactoring) throws IOException {
        if (reportFileName == null || reportFileName.isEmpty()) {
            ConsoleRefactorReportGenerator reportGenerator = new ConsoleRefactorReportGenerator();
            reportGenerator.generateReport(program, refactorSequence.getExecutedRefactorings());
        } else if (FilenameUtils.getExtension(reportFileName).equals("csv")) {
            CSVRefactorReportGenerator reportGenerator = new CSVRefactorReportGenerator(reportFileName, refactoredPath, refactorSequence.getFitnessMap().keySet());
            reportGenerator.generateReport(program, refactorSequence, POPULATION_SIZE, MAX_GEN, hyperVolume, iteration,
                    programExtractionTime, refactoringSearchTime, fitnessValuesWithoutRefactoring);
            reportGenerator.close();
        } else {
            throw new IllegalArgumentException("Unknown file type: " + reportFileName);
        }
    }

    private void createNewProjectFileWithCounterPostfix(File fileEntry, Program program, int counterPostfix) throws IOException {
        String outputPath = refactoredPath == null ? fileEntry.getParent() : refactoredPath;
        File folder = new File(outputPath);
        if (!folder.exists()) {
            Path folderPath = Paths.get(outputPath);
            Files.createDirectory(folderPath);
        }
        if ((FilenameUtils.getExtension(fileEntry.getPath())).equalsIgnoreCase("json")) {
            JSONFileCreator.writeJsonFromProgram(program, outputPath, "_refactored_" + counterPostfix);
        } else {
            JSONFileCreator.writeSb3FromProgram(program, outputPath, fileEntry, "_refactored_" + counterPostfix);
        }
    }
}
