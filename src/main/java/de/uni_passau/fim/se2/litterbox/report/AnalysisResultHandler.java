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
import java.util.Set;

public interface AnalysisResultHandler {
    void begin() throws IOException;

    void handleResult(Path projectFile, Program program, Set<Issue> result);

    void finish() throws IOException;

    static AnalysisResultHandler create(
            IssueTranslator translator,
            Path output,
            String detectors,
            boolean outputPerScript
    ) {
        if (output != null && (output.getFileName().toString().endsWith(".html") || output.getFileName().toString().endsWith(".htm"))) {
            return new HtmlAggregatedResultHandler(translator, output);
        }
        return new ImmediateResultHandler(translator, output, detectors, outputPerScript);
    }
}
