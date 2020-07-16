/*
 * Copyright (C) 2020 LitterBox contributors
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;


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
        for (String finder : detectors) {
            long numIssuesForFinder = issues.stream().filter(i -> i.getFinderShortName().equals(finder)).count();
            row.add(Long.toString(numIssuesForFinder));
        }
        printer.printRecord(row);
        printer.flush();
    }

    public void close() throws IOException {
        printer.close();
    }

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
