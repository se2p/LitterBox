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
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class BugAnalyzer extends Analyzer {

    private static final Logger log = Logger.getLogger(BugAnalyzer.class.getName());
    private List<String> detectorNames;
    private List<IssueFinder> issueFinders;
    private String annotationOutput;
    private boolean ignoreLooseBlocks;

    public BugAnalyzer(String input, String output, String detectors, boolean ignoreLooseBlocks, boolean delete) {
        super(input, output, delete);
        issueFinders = IssueTool.getFinders(detectors);
        detectorNames = issueFinders.stream().map(IssueFinder::getName).collect(Collectors.toList());
        this.ignoreLooseBlocks = ignoreLooseBlocks;
    }

    public void setAnnotationOutput(String annotationOutput) {
        this.annotationOutput = annotationOutput;
    }

    /**
     * The method for analyzing one Scratch project file (ZIP). It will produce only console output.
     *
     * @param fileEntry the file to analyze
     * @param reportFileName the file in which to write the results
     */
    void check(File fileEntry, String reportFileName) {
        Program program = extractProgram(fileEntry);
        if (program == null) {
            // Todo error message
            return;
        }

        Set<Issue> issues = runFinders(program);
        generateOutput(program, issues, reportFileName);
        createAnnotatedFile(fileEntry, program, issues, annotationOutput);
    }

    private Set<Issue> runFinders(Program program) {
        Preconditions.checkNotNull(program);
        Set<Issue> issues = new LinkedHashSet<>();
        for (IssueFinder iF : issueFinders) {
            iF.setIgnoreLooseBlocks(ignoreLooseBlocks);
            issues.addAll(iF.check(program));
        }
        return issues;
    }

    private void generateOutput(Program program, Set<Issue> issues, String reportFileName) {
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

    }

    private void createAnnotatedFile(File fileEntry, Program program, Set<Issue> issues, String annotatePath)  {
        if (annotationOutput != null && !annotationOutput.isEmpty()) {
            try {
                CommentGenerator commentGenerator = new CommentGenerator();
                commentGenerator.generateReport(program, issues);
                if ((FilenameUtils.getExtension(fileEntry.getPath())).toLowerCase().equals("json")) {
                    JSONFileCreator.writeJsonFromProgram(program, annotatePath);
                } else {
                    JSONFileCreator.writeSb3FromProgram(program, annotatePath, fileEntry);
                }
            } catch (IOException e) {
                log.warning(e.getMessage());
            }
        }
    }
}


