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

import de.uni_passau.fim.se2.litterbox.analytics.bugpattern.*;
import de.uni_passau.fim.se2.litterbox.analytics.fix_heuristics.*;
import de.uni_passau.fim.se2.litterbox.analytics.smells.StutteringMovement;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.parser.IssueParser;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
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
import java.util.*;
import java.util.logging.Logger;

public class BugAnalyzer extends Analyzer<Set<Issue>> {

    private static final Logger log = Logger.getLogger(BugAnalyzer.class.getName());

    private static final String ANNOTATED_PROGRAM_SUFFIX = "_annotated";

    private final List<String> detectorNames;
    private final List<IssueFinder> issueFinders;
    private Path annotationOutput;
    private final boolean ignoreLooseBlocks;
    private final boolean outputPerScript;

    private Path priorResultPath;

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
        if (priorResultPath != null) {
            Map<String, List<IssueParser.IssueRecord>> oldResults = readPriorIssues();
            Set<Issue> fixed = checkIfPriorIssuesFixed(program, issues, oldResults);
            issues.addAll(fixed);
        }
        return issues;
    }

    @Override
    protected void writeResultToFile(Path projectFile, Program program, Set<Issue> result) {
        generateOutput(program, result, output, outputPerScript);
        createAnnotatedFile(projectFile.toFile(), program, result, annotationOutput);
    }

    private Set<Issue> checkIfPriorIssuesFixed(Program program, Set<Issue> result, Map<String, List<IssueParser.IssueRecord>> oldResults) {
        Map<String, List<Issue>> resultsByFinder = sortResults(result);
        Set<String> oldFinders = oldResults.keySet();
        Set<Issue> fixedIssues = new HashSet<>();
        for (String finder : oldFinders) {
            if (resultsByFinder.containsKey(finder)) {
                fixedIssues.addAll(compareLocations(finder, oldResults.get(finder), resultsByFinder.get(finder), program));
            } else {
                fixedIssues.addAll(checkOldFixed(finder, oldResults.get(finder), program));
            }
        }
        return fixedIssues;
    }

    private Set<Issue> compareLocations(String finder, List<IssueParser.IssueRecord> issueRecords, List<Issue> result, Program program) {
        Set<Issue> fixes = new HashSet<>();
        for (IssueParser.IssueRecord issueRecord : issueRecords) {
            boolean found = false;
            for (Issue currentIssue : result) {
                if (issueRecord.blockId().equals(AstNodeUtil.getBlockId(currentIssue.getCodeLocation()))) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                fixes.addAll(checkOldFixed(finder, issueRecord, program));
            }
        }
        return fixes;
    }

    private Set<Issue> checkOldFixed(String finder, List<IssueParser.IssueRecord> issueRecords, Program program) {
        Set<Issue> issues = new HashSet<>();
        for (IssueParser.IssueRecord issueRecord : issueRecords) {
            issues.addAll(checkOldFixed(finder, issueRecord, program));
        }
        return issues;
    }

    private Set<Issue> checkOldFixed(String finder, IssueParser.IssueRecord issueRecord, Program program) {
        Set<Issue> issues = new HashSet<>();
        switch (finder) {
            case ComparingLiterals.NAME -> {
                ComparingLiteralsFix comparingLiteralsFix = new ComparingLiteralsFix(issueRecord.blockId());
                issues.addAll(comparingLiteralsFix.check(program));
            }
            case ForeverInsideLoop.NAME -> {
                ForeverInsideLoopFix foreverInsideLoopFix = new ForeverInsideLoopFix(issueRecord.actorName());
                issues.addAll(foreverInsideLoopFix.check(program));
            }
            case MessageNeverReceived.NAME -> {
                MessageNeverReceivedFix messageNeverReceived = new MessageNeverReceivedFix(issueRecord.blockId());
                issues.addAll(messageNeverReceived.check(program));
            }
            case MessageNeverSent.NAME -> {
                MessageNeverSentFix messageNeverSentFix = new MessageNeverSentFix(issueRecord.blockId());
                issues.addAll(messageNeverSentFix.check(program));
            }
            case MissingLoopSensing.NAME -> {
                MissingLoopSensingLoopFix missingLoopSensingLoopFix = new MissingLoopSensingLoopFix(issueRecord.blockId());
                issues.addAll(missingLoopSensingLoopFix.check(program));
                MissingLoopSensingWaitFix missingLoopSensingWaitFix = new MissingLoopSensingWaitFix(issueRecord.blockId());
                issues.addAll(missingLoopSensingWaitFix.check(program));
            }
            case StutteringMovement.NAME -> {
                StutteringMovementFix stutteringMovementFix = new StutteringMovementFix(issueRecord.blockId());
                issues.addAll(stutteringMovementFix.check(program));
            }
        }
        return issues;
    }

    private Map<String, List<Issue>> sortResults(Set<Issue> result) {
        Map<String, List<Issue>> resultsByFinder = new HashMap<>();
        for (Issue issue : result) {
            String finderName = issue.getFinderName();
            if (resultsByFinder.containsKey(finderName)) {
                resultsByFinder.get(finderName).add(issue);
            } else {
                List<Issue> sortedIssues = new ArrayList<>();
                sortedIssues.add(issue);
                resultsByFinder.put(finderName, sortedIssues);
            }
        }
        return resultsByFinder;
    }

    private Map<String, List<IssueParser.IssueRecord>> readPriorIssues() {
        File file = priorResultPath.toFile();
        if (file.exists()) {
            IssueParser parser = new IssueParser();
            try {
                return parser.parseFile(file);
            } catch (IOException e) {
                log.severe("Could not load program from file " + file.getName());
            } catch (ParsingException e) {
                log.severe("Could not parse program for file " + file.getName() + ". " + e.getMessage());
            }
            return null;
        } else {
            log.severe("File '" + file.getName() + "' does not exist");
            return null;
        }
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

    public void setPriorResultPath(Path priorResultPath) {
        this.priorResultPath = priorResultPath;
    }
}


