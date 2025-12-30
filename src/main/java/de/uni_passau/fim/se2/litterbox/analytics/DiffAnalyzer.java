/*
 * Copyright (C) 2019-2025 LitterBox contributors
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

import de.uni_passau.fim.se2.litterbox.analytics.fix_heuristics.FixHeuristicRegistry;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.parser.Scratch3Parser;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.report.IssueDTO;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Analyzes differences between two versions of a Scratch project.
 *
 * <p>Compares issues found in the "before" version with issues in the "after" version,
 * categorizing them into FIXED, NEW, PERSISTING, and REMOVED.
 */
public class DiffAnalyzer {

    private final String detectors;
    private final boolean ignoreLooseBlocks;
    private final Scratch3Parser parser;

    /**
     * Creates a new DiffAnalyzer.
     *
     * @param detectors comma-separated list of detector names or groups
     * @param ignoreLooseBlocks whether to ignore loose blocks during analysis
     */
    public DiffAnalyzer(String detectors, boolean ignoreLooseBlocks) {
        this.detectors = detectors;
        this.ignoreLooseBlocks = ignoreLooseBlocks;
        this.parser = new Scratch3Parser();
    }

    /**
     * Analyzes the diff between two project versions.
     *
     * @param beforePath path to the before version (.sb3 or .json report)
     * @param afterPath path to the after version (.sb3 only)
     * @return categorized diff results
     * @throws IOException if files cannot be read
     * @throws ParsingException if files cannot be parsed
     */
    public DiffResult analyze(Path beforePath, Path afterPath) throws IOException, ParsingException {
        String beforeSource = beforePath.toString();
        String afterSource = afterPath.toString();

        // Parse the after program (always .sb3)
        Program afterProgram = parser.parseFile(afterPath.toFile());

        // Load before issues - either from JSON report or by analyzing .sb3
        Map<String, List<IssueDTO>> beforeIssuesByFinder = loadBeforeIssues(beforePath);

        // Analyze the after program
        Set<Issue> afterIssues = analyzeProgram(afterProgram);
        Map<String, List<Issue>> afterIssuesByFinder = groupByFinder(afterIssues);

        // Categorize issues
        List<Issue> fixed = new ArrayList<>();
        List<Issue> persisting = new ArrayList<>();
        List<RemovedIssueInfo> removedInfos = new ArrayList<>();

        // Process each finder from before version
        for (Map.Entry<String, List<IssueDTO>> entry : beforeIssuesByFinder.entrySet()) {
            String finderName = entry.getKey();
            List<IssueDTO> beforeFinderIssues = entry.getValue();
            List<Issue> afterFinderIssues = afterIssuesByFinder.getOrDefault(finderName, List.of());

            categorizeIssues(
                    finderName,
                    beforeFinderIssues,
                    afterFinderIssues,
                    afterProgram,
                    fixed,
                    persisting,
                    removedInfos
            );
        }

        // Find NEW issues (in after but not in before)
        Set<String> beforeBlockIds = extractBlockIds(beforeIssuesByFinder);
        List<Issue> newIssues = afterIssues.stream()
                .filter(issue -> !beforeBlockIds.contains(getBlockId(issue)))
                .toList();

        // Convert removed info to placeholder issues for consistent API
        List<Issue> removed = createRemovedIssues(removedInfos, afterProgram);

        return new DiffResult(beforeSource, afterSource, fixed, newIssues, persisting, removed);
    }

    private Map<String, List<IssueDTO>> loadBeforeIssues(Path beforePath)
            throws IOException, ParsingException {
        if (isJsonFile(beforePath)) {
            return loadIssuesFromJson(beforePath);
        } else {
            return analyzeAndConvertToDTO(beforePath);
        }
    }

    private void categorizeIssues(
            String finderName,
            List<IssueDTO> beforeIssues,
            List<Issue> afterIssues,
            Program afterProgram,
            List<Issue> fixed,
            List<Issue> persisting,
            List<RemovedIssueInfo> removed
    ) {
        Set<String> afterBlockIds = afterIssues.stream()
                .map(this::getBlockId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        for (IssueDTO beforeIssue : beforeIssues) {
            String blockId = beforeIssue.issueLocationBlockId();
            if (blockId == null || blockId.isEmpty()) {
                continue;
            }

            if (afterBlockIds.contains(blockId)) {
                // Issue still exists at same location - PERSISTING
                afterIssues.stream()
                        .filter(i -> blockId.equals(getBlockId(i)))
                        .findFirst()
                        .ifPresent(persisting::add);
            } else {
                // Issue no longer at this location - check if fixed or just removed
                List<Issue> fixResults = checkIfFixed(finderName, blockId, afterProgram);
                if (!fixResults.isEmpty()) {
                    fixed.addAll(fixResults);
                } else {
                    removed.add(new RemovedIssueInfo(finderName, beforeIssue.sprite(), blockId));
                }
            }
        }
    }

    private List<Issue> checkIfFixed(String finderName, String blockId, Program program) {
        List<IssueFinder> heuristics = FixHeuristicRegistry.getFixHeuristics(finderName, blockId);
        List<Issue> fixIssues = new ArrayList<>();

        for (IssueFinder heuristic : heuristics) {
            Set<Issue> results = heuristic.check(program);
            fixIssues.addAll(results);
        }

        return fixIssues;
    }

    private List<Issue> createRemovedIssues(List<RemovedIssueInfo> removedInfos, Program program) {
        // For removed issues, we use IssueBuilder to create proper Issue objects
        List<Issue> removed = new ArrayList<>();
        for (RemovedIssueInfo info : removedInfos) {
            Issue issue = new IssueBuilder()
                    .withFinder(RemovedIssueFinder.INSTANCE)
                    .withProgram(program)
                    .withActor(program.getActorDefinitionList().getDefinitions().get(0))
                    .withSeverity(IssueSeverity.LOW)
                    .withHint("removed_issue")
                    .build();
            removed.add(issue);
        }
        return removed;
    }

    private Set<Issue> analyzeProgram(Program program) {
        ProgramBugAnalyzer analyzer = new ProgramBugAnalyzer(detectors, ignoreLooseBlocks);
        return analyzer.analyze(program);
    }

    private Map<String, List<IssueDTO>> analyzeAndConvertToDTO(Path projectPath)
            throws IOException, ParsingException {
        Program program = parser.parseFile(projectPath.toFile());
        Set<Issue> issues = analyzeProgram(program);

        Map<String, List<IssueDTO>> result = new HashMap<>();
        for (Issue issue : issues) {
            String finderName = issue.getFinderName();
            IssueDTO dto = createMinimalDTO(issue);
            result.computeIfAbsent(finderName, k -> new ArrayList<>()).add(dto);
        }
        return result;
    }

    private IssueDTO createMinimalDTO(Issue issue) {
        return new IssueDTO(
                issue.getId(),
                issue.getFinderName(),
                issue.getFinderName(),
                issue.getIssueType(),
                issue.getSeverity().ordinal(),
                issue.getActorName(),
                getBlockId(issue),
                null, null, null, null,
                null, null, 0, null, null
        );
    }

    private Map<String, List<IssueDTO>> loadIssuesFromJson(Path jsonPath)
            throws IOException, ParsingException {
        File file = jsonPath.toFile();
        IssueParser issueParser = new IssueParser();
        return issueParser.getIssuesPerFinder(file);
    }

    private boolean isJsonFile(Path path) {
        return path.toString().toLowerCase().endsWith(".json");
    }

    private Map<String, List<Issue>> groupByFinder(Set<Issue> issues) {
        return issues.stream().collect(Collectors.groupingBy(Issue::getFinderName));
    }

    private String getBlockId(Issue issue) {
        return AstNodeUtil.getBlockId(issue.getCodeLocation());
    }

    private Set<String> extractBlockIds(Map<String, List<IssueDTO>> issuesByFinder) {
        return issuesByFinder.values().stream()
                .flatMap(List::stream)
                .map(IssueDTO::issueLocationBlockId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    /** Info about a removed issue that couldn't be verified as fixed. */
    private record RemovedIssueInfo(String finderName, String spriteName, String blockId) {
    }

    /** Synthetic finder for representing removed issues. */
    private static class RemovedIssueFinder extends AbstractIssueFinder {
        static final RemovedIssueFinder INSTANCE = new RemovedIssueFinder();

        @Override
        public String getName() {
            return "removed_issue";
        }

        @Override
        public IssueType getIssueType() {
            return IssueType.BUG;
        }
    }
}
