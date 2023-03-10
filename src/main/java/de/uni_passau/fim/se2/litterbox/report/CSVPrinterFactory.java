/*
 * Copyright (C) 2019-2022 LitterBox contributors
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

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;

public class CSVPrinterFactory {
    private CSVPrinterFactory() {
        throw new IllegalCallerException("utility class");
    }

    /**
     * Creates a new CSV printer that either appends rows to the output file if it already exists, or creates a new file.
     *
     * @param outputFile The path to the file which the data should be written to.
     * @param heads      The header column names of the CSV data.
     * @return A CSV printer that writes to {@code outputFile}.
     * @throws IOException Thrown in case opening the target file is not possible.
     */
    public static CSVPrinter getNewPrinter(final String outputFile, final List<String> heads) throws IOException {
        final Path outputPath = Path.of(outputFile);
        final boolean outFileAlreadyExists = Files.exists(outputPath) && outputPath.toFile().length() > 0;

        final CSVFormat format;
        if (outFileAlreadyExists) {
            final List<String> existingHeaders = getHeaderNames(outputPath);
            if (heads.equals(existingHeaders)) {
                format = CSVFormat.DEFAULT.builder()
                        .setSkipHeaderRecord(true)
                        .build();
            } else {
                throw new IOException("File already exists with a different format.");
            }
        } else {
            format = CSVFormat.DEFAULT.builder()
                    .setHeader(heads.toArray(new String[0]))
                    .build();
        }

        final var writer = Files.newBufferedWriter(outputPath, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        return new CSVPrinter(writer, format);
    }

    private static List<String> getHeaderNames(final Path csvFile) throws IOException {
        try (BufferedReader br = Files.newBufferedReader(csvFile)) {
            final String headers = br.readLine();
            final String[] headerNames = headers.split(",");
            return Arrays.asList(headerNames);
        }
    }
}
