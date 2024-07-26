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
package de.uni_passau.fim.se2.litterbox.ast.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.report.IssueDTO;
import de.uni_passau.fim.se2.litterbox.report.ReportDTO;
import de.uni_passau.fim.se2.litterbox.utils.PropertyLoader;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class IssueParser {

    public ReportDTO parseIssueReport(final File issueReport) throws IOException, ParsingException {
        if (!FilenameUtils.getExtension(issueReport.getName()).equalsIgnoreCase("json")) {
            throw new ParsingException("This file type is not supported for issue reports.");
        }

        ObjectMapper mapper = new ObjectMapper();

        ReportDTO report = mapper.readValue(issueReport, ReportDTO.class);
        if (report == null) {
            throw new ParsingException("The JSON File is not a valid report.");
        }

        return report;
    }

    public Map<String, List<IssueDTO>> getIssuesPerFinder(final File file) throws IOException, ParsingException {
        final String fileName = file.getName();
        if (PropertyLoader.getSystemBooleanProperty("parser.log_file_name")) {
            Logger.getGlobal().info("Now parsing issue report: " + fileName);
        }

        final ReportDTO report = parseIssueReport(file);
        return extractIssuesPerFinder(report);
    }

    private Map<String, List<IssueDTO>> extractIssuesPerFinder(final ReportDTO report)  {
        return report.issues().stream().collect(Collectors.groupingBy(IssueDTO::finder));
    }
}

