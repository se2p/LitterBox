package de.uni_passau.fim.se2.litterbox.report;

import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;


public class CSVReportGenerator implements ReportGenerator {

    private String fileName;
    private List<String> headers = new ArrayList<>();
    private List<String> detectors;
    private CSVPrinter printer;

    public CSVReportGenerator(String fileName, String[] detectors) throws IOException {
        this.fileName = fileName;
        this.detectors = Arrays.asList(detectors);
        headers.add("project");
        this.detectors.stream().forEach(headers::add);
        printer = getNewPrinter(fileName, headers);
    }

    @Override
    public void generateReport(Program program, Collection<Issue> issues) throws IOException {

        List<String> row = new ArrayList<>();
        row.add(program.getIdent().getName());
        for(String finder : detectors) {
            long numIssuesForFinder = issues.stream().filter(i -> i.getFinderName().equals(finder)).count();
            row.add(Long.toString(numIssuesForFinder));
        }
        printer.printRecord(row);
        printer.flush();
    }

    private CSVPrinter getNewPrinter(String name, List<String> heads) throws IOException {

        if (Files.exists(Paths.get(name))) {
            BufferedWriter writer = Files.newBufferedWriter(
                    Paths.get(name), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            return new CSVPrinter(writer, CSVFormat.DEFAULT.withSkipHeaderRecord());
        } else {
            BufferedWriter writer = Files.newBufferedWriter(
                    Paths.get(name), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            return new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(heads.toArray(new String[0])));
        }
    }
}
