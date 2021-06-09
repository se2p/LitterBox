package de.uni_passau.fim.se2.litterbox.report;

import de.uni_passau.fim.se2.litterbox.analytics.RefactoringFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.refactor.RefactoringTool;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.RefactorSequence;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions.FitnessFunction;
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
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CSVRefactorReportGenerator {
    private final List<String> headers = new ArrayList<>();
    private final List<String> refactorings;
    private final CSVPrinter printer;

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
        List<String> fitnessFunctionsNames = fitnessFunctions.stream().map(FitnessFunction::getName).collect(Collectors.toList());
        headers.add("project");
        headers.add("population_size");
        headers.add("max_generations");
        headers.add("executed_generations");
        headers.add("seed");
        headers.add("hypervolume");
        headers.addAll(refactorings);
        headers.addAll(fitnessFunctionsNames);
        printer = getNewPrinter(fileName, refactoredPath);
    }

    public void generateReport(Program program, RefactorSequence refactorSequence, int populationSize, int maxGen,
                               double hyperVolume, int iteration) throws IOException {

        List<String> row = new ArrayList<>();
        row.add(program.getIdent().getName());
        row.add(String.valueOf(populationSize));
        row.add(String.valueOf(maxGen));
        row.add(String.valueOf(iteration));
        row.add(String.valueOf(Randomness.getSeed()));
        row.add(String.valueOf(hyperVolume));

        refactorings.stream().mapToLong(refactoring -> refactorSequence.getExecutedRefactorings()
                .stream()
                .filter(i -> i.getName().equals(refactoring))
                .count()).mapToObj(Long::toString).forEach(row::add);
        refactorSequence.getFitnessMap().values().stream().map(String::valueOf).forEach(row::add);
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
            try {
                Files.createDirectory(filePath.getParent());
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
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
}
