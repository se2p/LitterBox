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
package de.uni_passau.fim.se2.litterbox.analytics;

import de.uni_passau.fim.se2.litterbox.analytics.fix_heuristics.FixHeuristic;
import de.uni_passau.fim.se2.litterbox.analytics.fix_heuristics.FixHeuristicRegistry;
import de.uni_passau.fim.se2.litterbox.analytics.fix_heuristics.FixResult;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.report.IssueDTO;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Categorizes issues between two versions of a program as FIXED, NEW, PERSISTING, or REMOVED.
 *
 * <p>This class encapsulates the logic for comparing issues from a "before" version
 * with issues from an "after" version, determining what changed.
 */
public class IssueCategorizer {

    private final List<FixResult> fixed = new ArrayList<>();
    private final List<Issue> persisting = new ArrayList<>();
    private final List<Issue> newIssues = new ArrayList<>();
    private final List<RemovedIssueInfo> removed = new ArrayList<>();

    /**
     * Categorizes issues between before and after versions.
     *
     * @param beforeIssuesByFinder issues from the before version, grouped by finder name
     * @param afterIssues          all issues from the after version
     * @param afterProgram         the parsed after program (for fix verification)
     */
    public void categorize(
            Map<String, List<IssueDTO>> beforeIssuesByFinder,
            Set<Issue> afterIssues,
            Program afterProgram
    ) {
        Map<String, List<Issue>> afterIssuesByFinder = groupByFinder(afterIssues);
        Set<String> beforeBlockIds = extractAllBlockIds(beforeIssuesByFinder);

        // Process each finder from before version
        for (Map.Entry<String, List<IssueDTO>> entry : beforeIssuesByFinder.entrySet()) {
            String finderName = entry.getKey();
            List<IssueDTO> beforeFinderIssues = entry.getValue();
            List<Issue> afterFinderIssues = afterIssuesByFinder.getOrDefault(finderName, List.of());

            categorizeFinderIssues(finderName, beforeFinderIssues, afterFinderIssues, afterProgram);
        }

        // Find NEW issues (in after but not in before)
        for (Issue issue : afterIssues) {
            String blockId = getBlockId(issue);
            if (!isBlockIdInSet(blockId, beforeBlockIds)) {
                newIssues.add(issue);
            }
        }
    }

    private void categorizeFinderIssues(
            String finderName,
            List<IssueDTO> beforeIssues,
            List<Issue> afterIssues,
            Program afterProgram
    ) {
        Set<String> afterBlockIds = extractBlockIds(afterIssues);
        Set<String> afterActorNames = extractActorNames(afterIssues);

        for (IssueDTO beforeIssue : beforeIssues) {
            String blockId = beforeIssue.issueLocationBlockId();

            if (isBlockIdEmpty(blockId)) {
                // Issue without block ID - match by actor name instead
                categorizeByActorName(finderName, beforeIssue, afterIssues, afterActorNames, afterProgram);
            } else if (afterBlockIds.contains(blockId)) {
                // Issue still exists at same location - PERSISTING
                findIssueByBlockId(afterIssues, blockId).ifPresent(persisting::add);
            } else {
                // Issue no longer at this location - check if fixed or just removed
                categorizeAsFixedOrRemoved(finderName, beforeIssue, afterProgram);
            }
        }
    }

    private void categorizeByActorName(
            String finderName,
            IssueDTO beforeIssue,
            List<Issue> afterIssues,
            Set<String> afterActorNames,
            Program afterProgram
    ) {
        String actorName = beforeIssue.sprite();

        if (afterActorNames.contains(actorName)) {
            // Check if same finder+actor still has an issue
            findIssueByActorName(afterIssues, actorName).ifPresent(persisting::add);
        } else {
            // No issue for this actor anymore - check if fixed
            categorizeAsFixedOrRemoved(finderName, beforeIssue, afterProgram);
        }
    }

    private void categorizeAsFixedOrRemoved(String finderName, IssueDTO beforeIssue, Program afterProgram) {
        List<FixResult> fixResults = checkIfFixed(finderName, beforeIssue, afterProgram);
        if (!fixResults.isEmpty()) {
            fixed.addAll(fixResults);
        } else {
            String blockId = beforeIssue.issueLocationBlockId();
            removed.add(new RemovedIssueInfo(
                    finderName,
                    beforeIssue.sprite(),
                    isBlockIdEmpty(blockId) ? "N/A" : blockId
            ));
        }
    }

    private List<FixResult> checkIfFixed(String finderName, IssueDTO originalIssue, Program program) {
        List<FixHeuristic> heuristics = FixHeuristicRegistry.getFixHeuristics(finderName, originalIssue);
        List<FixResult> fixResults = new ArrayList<>();

        for (FixHeuristic heuristic : heuristics) {
            Set<FixResult> results = heuristic.check(program);
            fixResults.addAll(results);
        }

        return fixResults;
    }

    // ==================== Helper Methods ====================

    private boolean isBlockIdEmpty(String blockId) {
        return blockId == null || blockId.isEmpty();
    }

    private boolean isBlockIdInSet(String blockId, Set<String> blockIds) {
        return blockId != null && blockIds.contains(blockId);
    }

    private Optional<Issue> findIssueByBlockId(List<Issue> issues, String blockId) {
        return issues.stream()
                .filter(i -> blockId.equals(getBlockId(i)))
                .findFirst();
    }

    private Optional<Issue> findIssueByActorName(List<Issue> issues, String actorName) {
        return issues.stream()
                .filter(i -> actorName != null && actorName.equals(i.getActorName()))
                .findFirst();
    }

    private String getBlockId(Issue issue) {
        ASTNode codeLocation = issue.getCodeLocation();
        return codeLocation != null ? AstNodeUtil.getBlockId(codeLocation) : null;
    }

    private Set<String> extractBlockIds(List<Issue> issues) {
        return issues.stream()
                .map(this::getBlockId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private Set<String> extractActorNames(List<Issue> issues) {
        return issues.stream()
                .map(Issue::getActorName)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private Set<String> extractAllBlockIds(Map<String, List<IssueDTO>> issuesByFinder) {
        return issuesByFinder.values().stream()
                .flatMap(List::stream)
                .map(IssueDTO::issueLocationBlockId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private Map<String, List<Issue>> groupByFinder(Set<Issue> issues) {
        return issues.stream().collect(Collectors.groupingBy(Issue::getFinderName));
    }

    // ==================== Result Accessors ====================

    public List<FixResult> getFixed() {
        return Collections.unmodifiableList(fixed);
    }

    public List<Issue> getPersisting() {
        return Collections.unmodifiableList(persisting);
    }

    public List<Issue> getNewIssues() {
        return Collections.unmodifiableList(newIssues);
    }

    public List<RemovedIssueInfo> getRemoved() {
        return Collections.unmodifiableList(removed);
    }

    /**
     * Info about a removed issue that couldn't be verified as fixed.
     */
    public record RemovedIssueInfo(String finderName, String actorName, String blockId) {
    }
}
