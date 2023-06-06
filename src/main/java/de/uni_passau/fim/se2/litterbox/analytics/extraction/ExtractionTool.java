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
package de.uni_passau.fim.se2.litterbox.analytics.extraction;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.report.CSVPrinterFactory;
import org.apache.commons.csv.CSVPrinter;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ExtractionTool {

    private List<NameExtraction> getExtractors() {
        List<NameExtraction> extractorList = new ArrayList<>();

        extractorList.add(new BackdropNameExtraction());
        extractorList.add(new CostumeNameExtraction());
        extractorList.add(new ListNameExtraction());
        extractorList.add(new SpriteNameExtraction());
        extractorList.add(new VariableNameExtraction());
        return extractorList;
    }

    public List<String> getExtractorNames() {
        return getExtractors().stream().map(NameExtraction::getName).collect(Collectors.toList());
    }

    public List<NameExtraction> getAnalyzers() {
        return Collections.unmodifiableList(getExtractors());
    }

    public void createCSVFile(Program program, Path fileName) throws IOException {
        final List<String> headers = new ArrayList<>();
        headers.add("project");
        getExtractors().stream().map(NameExtraction::getName).forEach(headers::add);

        final List<String> row = new ArrayList<>();
        row.add(program.getIdent().getName());

        for (NameExtraction extractor : getExtractors()) {
            row.add(extractor.extractNames(program).toString());
        }

        try (CSVPrinter printer = CSVPrinterFactory.getNewPrinter(fileName, headers)) {
            printer.printRecord(row);
            printer.flush();
        }
    }
}
