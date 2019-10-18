/*
 * Copyright (C) 2019 LitterBox contributors
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
package utils;

import analytics.IssueReport;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import scratch.structure.Project;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Util class for writing and saving the csv
 */
public class CSVWriter {

    /**
     * Adds data to an existing CSVPrinter
     *
     * @param csvPrinter   the CSVPrinter to add the information
     * @param project      the project with the information
     * @param issueReports all the issueReports found in the project
     * @throws IOException corrupt file path
     */
    public static void addData(CSVPrinter csvPrinter, Project project, List<IssueReport> issueReports) throws IOException {
        List<String> data = new ArrayList<>();
        data.add(project.getName());
        for (IssueReport is : issueReports) {
            data.add(Integer.toString(is.getCount()));
        }
        csvPrinter.printRecord(data);
    }

    /**
     * Creates a new CSVPrinter with the correct head of all implemented issue names
     *
     * @return a new CSVPrinter
     * @throws IOException corrupt file path
     */
    public static CSVPrinter getNewPrinter(String name, List<String> heads) throws IOException {
            BufferedWriter writer = Files.newBufferedWriter(Paths.get(name));
            return new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(heads.toArray(new String[0])));
    }


    /**
     * Saves the file
     *
     * @param csvPrinter the CSVPrinter to save the data
     * @throws IOException corrupt file path
     */
    public static void flushCSV(CSVPrinter csvPrinter) throws IOException {
        csvPrinter.flush();
    }
}