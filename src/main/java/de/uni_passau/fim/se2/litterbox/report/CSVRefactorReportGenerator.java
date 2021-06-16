package de.uni_passau.fim.se2.litterbox.report;

import de.uni_passau.fim.se2.litterbox.analytics.RefactoringFinder;
import de.uni_passau.fim.se2.litterbox.analytics.metric.BlockCount;
import de.uni_passau.fim.se2.litterbox.analytics.metric.ScriptCount;
import de.uni_passau.fim.se2.litterbox.analytics.metric.WeightedMethodCountStrict;
import de.uni_passau.fim.se2.litterbox.analytics.smells.LongScript;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.refactor.RefactoringTool;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.algorithms.Dominance;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.RefactorSequence;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions.FitnessFunction;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators.Crossover;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators.Mutation;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators.RefactorSequenceCrossover;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators.RefactorSequenceMutation;
import de.uni_passau.fim.se2.litterbox.utils.Randomness;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CSVRefactorReportGenerator {
    private final List<String> headers = new ArrayList<>();
    private final List<String> refactorings;
    private final CSVPrinter printer;
    private final Set<FitnessFunction<RefactorSequence>> fitnessFunctions;

    /**
     * CSVRefactorReportGenerator writes the results of an analyses for a given list of refactorings to a file.
     *
     * @param fileName         of the file to which the report is written.
     * @param refactoredPath   the path of the file which the report is written
     * @param fitnessFunctions list of used FitnessFunctions.
     * @throws IOException is thrown if the file cannot be opened
     */
    public CSVRefactorReportGenerator(String fileName, String refactoredPath, Set<FitnessFunction<RefactorSequence>> fitnessFunctions) throws IOException {
        refactorings = RefactoringTool.getRefactoringFinders().stream().map(RefactoringFinder::getName).collect(Collectors.toList());
        List<String> fitnessFunctionsNamesWithoutRefactoring = fitnessFunctions.stream().map(fitnessFunction -> fitnessFunction.getName() + "_without_refactoring").collect(Collectors.toList());
        List<String> fitnessFunctionsNames = fitnessFunctions.stream().map(FitnessFunction::getName).collect(Collectors.toList());
        headers.add("project");
        headers.add("pareto_index");
        headers.add("population_size");
        headers.add("max_generations");
        headers.add("executed_generations");
        headers.add("seed");
        headers.add("hypervolume");
        headers.add("program_extraction_time");
        headers.add("refactoring_search_time");
        headers.addAll(refactorings);
        headers.add("blocks_without_refactoring");
        headers.add("blocks");
        headers.addAll(fitnessFunctionsNames);
        headers.addAll(fitnessFunctionsNamesWithoutRefactoring);
        headers.add("long_scripts_without_refactoring");
        headers.add("long_scripts");
        headers.add("scripts_without_refactoring");
        headers.add("scripts");
        headers.add("wmc_without_refactoring");
        headers.add("wmc");
        headers.add("dominated");
        printer = getNewPrinter(fileName, refactoredPath);
        this.fitnessFunctions = fitnessFunctions;
    }

    public void generateReport(int index, Program program, RefactorSequence refactorSequence, int populationSize, int maxGen,
                               double hyperVolume, int iteration, long programExtractionTime,
                               long refactoringSearchTime) throws IOException {

        List<String> row = new ArrayList<>();
        row.add(program.getIdent().getName());
        row.add(String.valueOf(index));
        row.add(String.valueOf(populationSize));
        row.add(String.valueOf(maxGen));
        row.add(String.valueOf(iteration));
        row.add(String.valueOf(Randomness.getSeed()));
        row.add(String.valueOf(hyperVolume));
        row.add(String.valueOf(programExtractionTime));
        row.add(String.valueOf(refactoringSearchTime));
        refactorings.stream().mapToLong(refactoring -> refactorSequence.getExecutedRefactorings()
                .stream()
                .filter(i -> i.getName().equals(refactoring))
                .count()).mapToObj(Long::toString).forEach(row::add);
        row.add(String.valueOf(new BlockCount<Program>().calculateMetric(refactorSequence.getOriginalProgram())));
        row.add(String.valueOf(new BlockCount<Program>().calculateMetric(refactorSequence.getRefactoredProgram())));
        refactorSequence.getFitnessMap().values().stream().map(String::valueOf).forEach(row::add);

        List<String> fitnessValuesWithoutRefactoring = new LinkedList<>();
        RefactorSequence emptyRefactorSequence = createEmptyRefactorSequence(refactorSequence.getOriginalProgram());
        for (FitnessFunction<RefactorSequence> fitnessFunction : fitnessFunctions) {
            String fitnessValueWithoutRefactoring = String.valueOf(fitnessFunction.getFitness(emptyRefactorSequence));
            fitnessValuesWithoutRefactoring.add(fitnessValueWithoutRefactoring);
        }

        row.addAll(fitnessValuesWithoutRefactoring);

        LongScript longScript = new LongScript();
        row.add(String.valueOf(longScript.check(refactorSequence.getOriginalProgram()).size()));
        row.add(String.valueOf(longScript.check(refactorSequence.getRefactoredProgram()).size()));

        ScriptCount<Program> scriptCount = new ScriptCount<>();
        row.add(String.valueOf(scriptCount.calculateMetric(refactorSequence.getOriginalProgram())));
        row.add(String.valueOf(scriptCount.calculateMetric(refactorSequence.getRefactoredProgram())));

        WeightedMethodCountStrict<Program> wmc = new WeightedMethodCountStrict<>();
        row.add(String.valueOf(wmc.calculateMetric(refactorSequence.getOriginalProgram())));
        row.add(String.valueOf(wmc.calculateMetric(refactorSequence.getRefactoredProgram())));

        Dominance<RefactorSequence> dominance = new Dominance<>(fitnessFunctions);
        row.add(dominance.test(refactorSequence, emptyRefactorSequence) ? "1" : "0");

        printer.printRecord(row);
        printer.flush();
    }

    public void close() throws IOException {
        printer.close();
    }

    protected CSVPrinter getNewPrinter(String name, String refactoredPath) throws IOException {
        File folder;
        Path filePath;
        Path namePath = Paths.get(name);
        if (namePath.isAbsolute()) {
            filePath = namePath;
            folder = new File(filePath.getParent().toString());
        } else {
            filePath = Paths.get(refactoredPath + System.getProperty("file.separator") + name);
            folder = new File(refactoredPath);
        }

        if (!folder.exists()) {
                Files.createDirectory(filePath.getParent());
        }

        if (filePath.toFile().length() > 0) {
            BufferedWriter writer = Files.newBufferedWriter(
                    filePath, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            return new CSVPrinter(writer, CSVFormat.DEFAULT.withSkipHeaderRecord());
        } else {
            BufferedWriter writer = Files.newBufferedWriter(
                    filePath, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            return new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(headers.toArray(new String[0])));
        }
    }

    private RefactorSequence createEmptyRefactorSequence(Program program) {
        Crossover<RefactorSequence> crossover = new RefactorSequenceCrossover();
        Mutation<RefactorSequence> mutation = new RefactorSequenceMutation(List.of());
        return new RefactorSequence(program, mutation, crossover, List.of(), List.of());
    }
}
