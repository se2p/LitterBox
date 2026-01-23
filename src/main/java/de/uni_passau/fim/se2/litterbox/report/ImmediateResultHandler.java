/*
 * Copyright (C) 2019-2026 LitterBox contributors
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
import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueTool;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslator;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class ImmediateResultHandler implements AnalysisResultHandler {

    private static final Logger log = Logger.getLogger(ImmediateResultHandler.class.getName());

    private final IssueTranslator translator;
    private final Path output;
    private final boolean outputPerScript;
    private final List<String> detectorNames;

    public ImmediateResultHandler(IssueTranslator translator, Path output, String detectors,
                                  boolean outputPerScript) {
        this.translator = translator;
        this.output = output;
        this.outputPerScript = outputPerScript;
        this.detectorNames = IssueTool.getFinders(detectors).stream().map(IssueFinder::getName).toList();
    }

    @Override
    public void begin() throws IOException {
        // Nothing to do for immediate handler
    }

    @Override
    public void handleResult(Path projectFile, Program program, Set<Issue> result) {
        List<String> detectorsToWrite = new ArrayList<>(detectorNames);
        try {
            if (output == null) {
                ConsoleReportGenerator reportGenerator = new ConsoleReportGenerator(detectorsToWrite);
                reportGenerator.generateReport(program, result);
            } else if (output.getFileName().toString().endsWith(".json")) {
                JSONReportGenerator reportGenerator = new JSONReportGenerator(translator, output);
                reportGenerator.generateReport(program, result);
            } else if (output.getFileName().toString().endsWith(".csv")) {
                try (CSVReportGenerator reportGenerator
                             = new CSVReportGenerator(output, detectorsToWrite, outputPerScript)
                ) {
                    reportGenerator.generateReport(program, result);
                }
            } else {
                throw new IllegalArgumentException("Unknown file type: " + output);
            }
        } catch (IOException e) {
            log.warning(e.getMessage());
        }
    }

    @Override
    public void finish() throws IOException {
        // Nothing to do for immediate handler
    }
}
