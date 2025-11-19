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

import de.uni_passau.fim.se2.litterbox.analytics.metric.MetricExtractor;
import de.uni_passau.fim.se2.litterbox.analytics.metric.MetricResult;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.report.CSVPrinterFactory;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class MetricAnalyzer extends FileAnalyzer<List<MetricResult>> {

    private static final Logger log = Logger.getLogger(MetricAnalyzer.class.getName());

    private final ProgramMetricAnalyzer analyzer;

    public MetricAnalyzer(Path output, boolean delete) {
        super(new ProgramMetricAnalyzer(), output, delete);
        this.analyzer = (ProgramMetricAnalyzer) super.analyzer;
    }

    @Override
    protected void checkAndWrite(File file) throws IOException {
        Program program = extractProgram(file);
        if (program == null) {
            return;
        }

        createCSVFile(program, output);
    }

    @Override
    protected void writeResultToFile(Path projectFile, Program program, List<MetricResult> checkResult)
            throws IOException {
        try {
            createCSVFile(program, output);
        } catch (IOException e) {
            log.warning("Could not create CSV file: " + output);
            throw e;
        }
    }

    private void createCSVFile(Program program, Path fileName) throws IOException {
        final List<String> headers = new ArrayList<>();
        headers.add("project");
        analyzer.getMetrics().stream().map(MetricExtractor::getName).forEach(headers::add);

        final List<String> row = new ArrayList<>();
        row.add(program.getIdent().getName());

        List<MetricResult> results = analyzer.analyze(program);
        for (MetricResult result : results) {
            row.add(Double.toString(result.value()));
        }

        if (fileName == null) {
            try (CSVPrinter printer = new CSVPrinter(System.out, CSVFormat.DEFAULT.builder().setHeader(headers.toArray(new String[0])).get())) {
                printer.printRecord(row);
                printer.flush();
            }
        } else {
            try (CSVPrinter printer = CSVPrinterFactory.getNewPrinter(fileName, headers)) {
                printer.printRecord(row);
                printer.flush();
            }
        }
    }
}
