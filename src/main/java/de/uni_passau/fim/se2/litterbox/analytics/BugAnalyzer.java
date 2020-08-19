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
package de.uni_passau.fim.se2.litterbox.analytics;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.jsonCreation.JSONFileCreator;
import de.uni_passau.fim.se2.litterbox.report.CSVReportGenerator;
import de.uni_passau.fim.se2.litterbox.report.CommentGenerator;
import de.uni_passau.fim.se2.litterbox.report.ConsoleReportGenerator;
import de.uni_passau.fim.se2.litterbox.report.JSONReportGenerator;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import static de.uni_passau.fim.se2.litterbox.utils.GroupConstants.*;

public class BugAnalyzer extends Analyzer {

    private static final Logger log = Logger.getLogger(BugAnalyzer.class.getName());
    private final IssueTool issueTool;
    private List<String> detectorNames;
    private String annotationOutput;

    public BugAnalyzer(String input, String output, String detectors) {
        super(input, output);
        this.issueTool = new IssueTool();
        setDetectorNames(detectors);
    }

    public void setDetectorNames(String detectorNames) {
        switch (detectorNames) {
            case ALL:
                this.detectorNames = new ArrayList<>(issueTool.getAllFinders().keySet());
                break;
            case BUGS:
                this.detectorNames = new ArrayList<>(issueTool.getBugFinders().keySet());
                break;
            case SMELLS:
                this.detectorNames = new ArrayList<>(issueTool.getSmellFinders().keySet());
                break;
            default:
                this.detectorNames = Arrays.asList(detectorNames.split(","));
                break;
        }
    }

    public void setAnnotationOutput(String annotationOutput) {
        this.annotationOutput = annotationOutput;
    }

    /**
     * The method for analyzing one Scratch project file (ZIP). It will produce only console output.
     *
     * @param fileEntry the file to analyze
     */
    void check(File fileEntry, String reportFileName) {
        Program program = extractProgram(fileEntry);
        if (program == null) {
            // Todo error message
            return;
        }

        Set<Issue> issues = issueTool.check(program, detectorNames);

        // TODO: Refactor error handling
        try {
            if (reportFileName == null || reportFileName.isEmpty()) {
                ConsoleReportGenerator reportGenerator = new ConsoleReportGenerator(detectorNames);
                reportGenerator.generateReport(program, issues);
            } else if (reportFileName.endsWith(".json")) {
                JSONReportGenerator reportGenerator = new JSONReportGenerator(reportFileName);
                reportGenerator.generateReport(program, issues);
            } else if (reportFileName.endsWith(".csv")) {
                CSVReportGenerator reportGenerator = new CSVReportGenerator(reportFileName, detectorNames);
                reportGenerator.generateReport(program, issues);
                reportGenerator.close();
            } else {
                throw new IllegalArgumentException("Unknown file type: " + reportFileName);
            }
        } catch (IOException e) {
            log.warning(e.getMessage());
        }

        if (annotationOutput != null && !annotationOutput.isEmpty()) {
            try {
                CommentGenerator commentGenerator = new CommentGenerator();
                commentGenerator.generateReport(program, issues);
                createAnnotatedFile(fileEntry, program, annotationOutput);
            } catch (IOException e) {
                log.warning(e.getMessage());
            }
        }
    }

    private void createAnnotatedFile(File fileEntry, Program program, String annotatePath) throws IOException {
        if ((FilenameUtils.getExtension(fileEntry.getPath())).toLowerCase().equals("json")) {
            JSONFileCreator.writeJsonFromProgram(program, annotatePath);
        } else {
            JSONFileCreator.writeSb3FromProgram(program, annotatePath, fileEntry);
        }
    }
}


