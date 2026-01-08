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
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslator;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class HtmlAggregatedResultHandler implements AnalysisResultHandler {

    private static final Logger log = Logger.getLogger(HtmlAggregatedResultHandler.class.getName());

    private final IssueTranslator translator;
    private final Path output;
    private final Map<String, ProjectAnalysisResult> htmlReportAccumulator = new HashMap<>();

    public HtmlAggregatedResultHandler(IssueTranslator translator, Path output) {
        this.translator = translator;
        this.output = output;
    }

    @Override
    public void begin() throws IOException {
        htmlReportAccumulator.clear();
    }

    @Override
    public void handleResult(Path projectFile, Program program, Set<Issue> result) {
        String projectName = extractProjectName(projectFile);
        htmlReportAccumulator.put(projectName, new ProjectAnalysisResult(projectName, program, result));
    }

    @Override
    public void finish() throws IOException {
        if (!htmlReportAccumulator.isEmpty()) {
            try {
                HTMLReportGenerator reportGenerator = new HTMLReportGenerator(translator, output);
                reportGenerator.generateReport(htmlReportAccumulator.values());
            } catch (IOException e) {
                log.warning("Failed to generate HTML report: " + e.getMessage());
            }
        }
    }

    private String extractProjectName(Path projectFile) {
        String fileName = projectFile.getFileName().toString();
        // Remove extension
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? fileName : fileName.substring(0, dotIndex);
    }
}
