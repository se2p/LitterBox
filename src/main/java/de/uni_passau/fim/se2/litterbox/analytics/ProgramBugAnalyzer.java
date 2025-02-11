/*
 * Copyright (C) 2019-2024 LitterBox contributors
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
import de.uni_passau.fim.se2.litterbox.analytics.smells.*;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.report.IssueDTO;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ProgramBugAnalyzer implements ProgramAnalyzer<Set<Issue>> {
    private static final Logger log = Logger.getLogger(ProgramBugAnalyzer.class.getName());
    private final List<IssueFinder> issueFinders;
    private final boolean ignoreLooseBlocks;
    private final Path priorResultsPath;

    public ProgramBugAnalyzer(final String detectors, final boolean ignoreLooseBlocks, final Path priorResultsPath) {
        this.issueFinders = IssueTool.getFinders(detectors);
        this.ignoreLooseBlocks = ignoreLooseBlocks;
        this.priorResultsPath = priorResultsPath;
    }

    public ProgramBugAnalyzer(final String detectors, final boolean ignoreLooseBlocks) {
        this.issueFinders = IssueTool.getFinders(detectors);
        this.ignoreLooseBlocks = ignoreLooseBlocks;
        this.priorResultsPath = null;
    }

    @Override
    public Set<Issue> analyze(Program program) {
        Preconditions.checkNotNull(program);
        Set<Issue> issues = new LinkedHashSet<>();
        for (IssueFinder issueFinder : issueFinders) {
            issueFinder.setIgnoreLooseBlocks(ignoreLooseBlocks);
            issues.addAll(issueFinder.check(program));
        }
        if (priorResultsPath != null) {
            Map<String, List<IssueDTO>> oldResults = readPriorIssues();
            Set<Issue> fixed = checkIfPriorIssuesFixed(program, issues, oldResults);
            issues.addAll(fixed);
        }
        return issues;
    }

    private Set<Issue> checkIfPriorIssuesFixed(Program program, Set<Issue> result, Map<String, List<IssueDTO>> oldResults) {
        Map<String, List<Issue>> resultsByFinder = sortResults(result);
        Set<Issue> fixedIssues = new HashSet<>();

        for (var entry : oldResults.entrySet()) {
            String finder = entry.getKey();
            List<IssueDTO> finderResults = entry.getValue();

            if (resultsByFinder.containsKey(finder)) {
                fixedIssues.addAll(compareLocations(finder, finderResults, resultsByFinder.get(finder), program));
            } else {
                fixedIssues.addAll(checkOldFixed(finder, finderResults, program));
            }
        }

        return fixedIssues;
    }

    private Set<Issue> compareLocations(String finder, List<IssueDTO> issueRecords, List<Issue> result, Program program) {
        Set<Issue> fixes = new HashSet<>();
        for (IssueDTO issueRecord : issueRecords) {
            if (!(issueRecord.finder().equals(NoWorkingScripts.NAME)) && !(issueRecord.finder().equals(DuplicateSprite.NAME))
                    && !(issueRecord.finder().equals(SpriteNaming.NAME)) && !(issueRecord.finder().equals(EmptyProject.NAME))
                    && !(issueRecord.finder().equals(SameVariableDifferentSprite.NAME)) && !(issueRecord.finder().equals(EmptySprite.NAME))
                    && !issueRecord.issueLocationBlockId().isEmpty()) {
                boolean found = false;
                for (Issue currentIssue : result) {
                    String issueBlockId = AstNodeUtil.getBlockId(currentIssue.getCodeLocation());
                    if (Objects.equals(issueBlockId, issueRecord.issueLocationBlockId())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    fixes.addAll(checkOldFixed(finder, issueRecord, program));
                }
            }
        }
        return fixes;
    }

    private Set<Issue> checkOldFixed(String finder, List<IssueDTO> issueRecords, Program program) {
        Set<Issue> issues = new HashSet<>();
        for (IssueDTO issueRecord : issueRecords) {
            issues.addAll(checkOldFixed(finder, issueRecord, program));
        }
        return issues;
    }

    private Set<Issue> checkOldFixed(String finder, IssueDTO issueRecord, Program program) {
        Set<Issue> issues = new HashSet<>();
        String location = issueRecord.issueLocationBlockId();

        switch (finder) {
            case ComparingLiterals.NAME -> {
                ComparingLiteralsFix comparingLiteralsFix = new ComparingLiteralsFix(location);
                issues.addAll(comparingLiteralsFix.check(program));
            }
            case ForeverInsideLoop.NAME -> {
                ForeverInsideLoopFix foreverInsideLoopFix = new ForeverInsideLoopFix(issueRecord.sprite());
                issues.addAll(foreverInsideLoopFix.check(program));
            }
            case MessageNeverReceived.NAME -> {
                MessageNeverReceivedFix messageNeverReceived = new MessageNeverReceivedFix(location);
                issues.addAll(messageNeverReceived.check(program));
            }
            case MessageNeverSent.NAME -> {
                MessageNeverSentFix messageNeverSentFix = new MessageNeverSentFix(location);
                issues.addAll(messageNeverSentFix.check(program));
            }
            case MissingCloneInitialization.NAME -> {
                MissingCloneInitializationFix missingCloneInitializationFix = new MissingCloneInitializationFix(location);
                issues.addAll(missingCloneInitializationFix.check(program));
            }
            case MissingLoopSensing.NAME -> {
                MissingLoopSensingLoopFix missingLoopSensingLoopFix = new MissingLoopSensingLoopFix(location);
                issues.addAll(missingLoopSensingLoopFix.check(program));
                MissingLoopSensingWaitFix missingLoopSensingWaitFix = new MissingLoopSensingWaitFix(location);
                issues.addAll(missingLoopSensingWaitFix.check(program));
            }
            case StutteringMovement.NAME -> {
                StutteringMovementFix stutteringMovementFix = new StutteringMovementFix(location);
                issues.addAll(stutteringMovementFix.check(program));
            }
            default -> {
                // do nothing since we do not have a fix check for this type
            }
        }
        return issues;
    }

    private Map<String, List<Issue>> sortResults(Set<Issue> result) {
        return result.stream().collect(Collectors.groupingBy(Issue::getFinderName));
    }

    private Map<String, List<IssueDTO>> readPriorIssues() {
        File file = priorResultsPath.toFile();
        if (file.exists()) {
            IssueParser parser = new IssueParser();
            try {
                return parser.getIssuesPerFinder(file);
            } catch (IOException e) {
                log.severe("Could not load issues from file " + file.getName());
                throw new RuntimeException("Could not load issues from file " + file.getName());
            } catch (ParsingException e) {
                log.severe("Could not parse issues for file " + file.getName() + ". " + e.getMessage());
                throw new RuntimeException("Could not parse issues for file " + file.getName() + ". " + e.getMessage());
            }
        } else {
            log.severe("File '" + file.getName() + "' does not exist");
            throw new RuntimeException("Could not load issues from file " + file.getName());
        }
    }
}
