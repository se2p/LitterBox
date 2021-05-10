package de.uni_passau.fim.se2.litterbox.report;

import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.RefactorSequence;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.Refactoring;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class CSVRefactorReportGenerator {
    private List<String> headers = new ArrayList<>();
    private List<String> refactorings;
    private CSVPrinter printer;

    /**
     * CSVReportGenerator writes the results of an analyses for a given list of refactorings to a file.
     *
     * @param fileName     of the file to which the report is written.
     * @param refactorings list of Refactorings that should be included in the report.
     * @throws IOException is thrown if the file cannot be opened
     */
    public CSVRefactorReportGenerator(String fileName, List<Refactoring> refactorings) throws IOException {
        this.refactorings = refactorings.stream().map(Refactoring::getName).distinct().collect(Collectors.toList());
        headers.add("project");
        headers.addAll(this.refactorings);
        printer = getNewPrinter(fileName);
    }

    public void generateReport(Program program, RefactorSequence refactorSequence, int populationSize, int maxGen) throws IOException {

        List<String> row = new ArrayList<>();
        row.add(program.getIdent().getName());
        for (String refactoring : refactorings) {
            long numIssuesForFinder = refactorSequence.getExecutedRefactorings()
                    .stream()
                    .filter(i -> i.getName().equals(refactoring))
                    .count();
            row.add(Long.toString(numIssuesForFinder));
        }
        printer.printRecord(row);
        printer.flush();
    }

    public void close() throws IOException {
        printer.close();
    }

    protected CSVPrinter getNewPrinter(String name) throws IOException {

        Path filePath = Paths.get(name);
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
