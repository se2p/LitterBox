package de.uni_passau.fim.se2.litterbox.analytics;


import de.uni_passau.fim.se2.litterbox.analytics.metric.*;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MetricAnalyzer {

    private List<MetricExtractor> metrics = Arrays.asList(
            new BlockCount(),
            new SpriteCount(),
            new ProcedureCount(),
            new ProgramUsingPen(),
            new WeightedMethodCount());

    public List<String> getMetricNames() {
        return metrics.stream().map(MetricExtractor::getName).collect(Collectors.toList());
    }

    public void createCSVFile(Program program, String fileName) throws IOException {
        List<String> headers = new ArrayList<>();
        headers.add("project");
        metrics.stream().map(MetricExtractor::getName).forEach(headers::add);
        CSVPrinter printer = getNewPrinter(fileName, headers);
        List<String> row = new ArrayList<>();
        row.add(program.getIdent().getName());

        for(MetricExtractor extractor : metrics) {
            row.add(Double.toString(extractor.calculateMetric(program)));
        }
        printer.printRecord(row);
        printer.flush();
    }

    // TODO: Code clone -- same is in CSVReportGenerator
    protected CSVPrinter getNewPrinter(String name, List<String> heads) throws IOException {

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
