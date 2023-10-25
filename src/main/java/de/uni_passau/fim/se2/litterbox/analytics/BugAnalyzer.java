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
package de.uni_passau.fim.se2.litterbox.analytics;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.jsoncreation.JSONFileCreator;
import de.uni_passau.fim.se2.litterbox.report.CSVReportGenerator;
import de.uni_passau.fim.se2.litterbox.report.CommentGenerator;
import de.uni_passau.fim.se2.litterbox.report.ConsoleReportGenerator;
import de.uni_passau.fim.se2.litterbox.report.JSONReportGenerator;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class BugAnalyzer extends Analyzer<Set<Issue>> {

    private static final Logger log = Logger.getLogger(BugAnalyzer.class.getName());

    private static final String ANNOTATED_PROGRAM_SUFFIX = "_annotated";

    private final List<String> detectorNames;
    private final List<IssueFinder> issueFinders;
    private Path annotationOutput;
    private final boolean ignoreLooseBlocks;
    private final boolean outputPerScript;

    public BugAnalyzer(
            Path input, Path output, String detectors,
            boolean ignoreLooseBlocks, boolean delete, boolean outputPerScript
    ) {
        super(input, output, delete);

        this.issueFinders = IssueTool.getFinders(detectors);
        this.outputPerScript = outputPerScript;
        this.detectorNames = issueFinders.stream().map(IssueFinder::getName).toList();
        this.ignoreLooseBlocks = ignoreLooseBlocks;
    }

    public void setAnnotationOutput(Path annotationOutput) {
        this.annotationOutput = annotationOutput;
    }

    @Override
    public Set<Issue> check(Program program) {
        Preconditions.checkNotNull(program);
        Set<Issue> issues = new LinkedHashSet<>();
        for (IssueFinder issueFinder : issueFinders) {
            issueFinder.setIgnoreLooseBlocks(ignoreLooseBlocks);
            issues.addAll(issueFinder.check(program));
        }
        return issues;
    }

    @Override
    protected void writeResultToFile(Path projectFile, Program program, Set<Issue> result) {
        generateOutput(program, result, output, outputPerScript);
        createAnnotatedFile(projectFile.toFile(), program, result, annotationOutput);
    }

    private void generateOutput(Program program, Set<Issue> issues, Path reportFileName, boolean outputPerScript) {
        try {
            if (reportFileName == null) {
                ConsoleReportGenerator reportGenerator = new ConsoleReportGenerator(detectorNames);
                reportGenerator.generateReport(program, issues);
            } else if (reportFileName.getFileName().toString().endsWith(".json")) {
                JSONReportGenerator reportGenerator = new JSONReportGenerator(reportFileName);
                reportGenerator.generateReport(program, issues);
            } else if (reportFileName.getFileName().toString().endsWith(".csv")) {
                try (CSVReportGenerator reportGenerator
                             = new CSVReportGenerator(reportFileName, detectorNames, outputPerScript)
                ) {
                    reportGenerator.generateReport(program, issues);
                }
            } else {
                throw new IllegalArgumentException("Unknown file type: " + reportFileName);
            }
        } catch (IOException e) {
            log.warning(e.getMessage());
        }
    }

    private void createAnnotatedFile(File fileEntry, Program program, Set<Issue> issues, Path annotatePath) {
        if (annotationOutput == null) {
            return;
        }

        try {
            CommentGenerator commentGenerator = new CommentGenerator();
            commentGenerator.generateReport(program, issues);
            String fileExtension = FilenameUtils.getExtension(fileEntry.getPath());

            if (fileExtension.equalsIgnoreCase("json")) {
                JSONFileCreator.writeJsonFromProgram(program, annotatePath, ANNOTATED_PROGRAM_SUFFIX);
            } else if (fileExtension.equalsIgnoreCase("sb3")) {
                JSONFileCreator.writeSb3FromProgram(program, annotatePath, fileEntry, ANNOTATED_PROGRAM_SUFFIX);
            } else {
                JSONFileCreator.writeMBlockFromProgram(program, annotatePath, fileEntry, ANNOTATED_PROGRAM_SUFFIX);
            }
        } catch (IOException e) {
            log.warning(e.getMessage());
        }
    }
}


