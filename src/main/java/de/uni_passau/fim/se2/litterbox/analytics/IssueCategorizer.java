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
import de.uni_passau.fim.se2.litterbox.ast.util.BlockIdMapper;
import de.uni_passau.fim.se2.litterbox.report.IssueDTO;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslator;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslatorFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Categorizes issues between two versions of a program as FIXED, NEW, PERSISTING, or REMOVED.
 *
 * <p>Uses a 3-pass algorithm to match issues between versions:
 * <ol>
 *   <li>Pass 1: Match by exact block ID (fast path for unchanged IDs)</li>
 *   <li>Pass 2: Match via structural block ID mapping (for re-serialized programs)</li>
 *   <li>Pass 3: Hint fallback and fix heuristics for remaining unmatched issues</li>
 * </ol>
 */
public class IssueCategorizer {

    private final List<Issue> fixed = new ArrayList<>();
    private final List<Issue> persisting = new ArrayList<>();
    private final List<Issue> newIssues = new ArrayList<>();
    private final List<RemovedIssueInfo> removed = new ArrayList<>();

    /**
     * Tracks which after issues have been matched to prevent double-matching.
     */
    private final Set<Issue> matchedAfterIssues = new HashSet<>();

    /**
     * Tracks which before issues have been matched in the current finder.
     */
    private Set<IssueDTO> matchedBeforeIssues;

    private final IssueTranslator translator;

    public IssueCategorizer() {
        this(IssueTranslatorFactory.getIssueTranslator());
    }

    public IssueCategorizer(IssueTranslator translator) {
        this.translator = translator;
    }

    /**
     * Categorizes issues between before and after versions.
     *
     * @param beforeIssuesByFinder issues from the before version, grouped by finder name
     * @param afterIssues          all issues from the after version
     * @param beforeProgram        the parsed before program (may be null if loaded from JSON report)
     * @param afterProgram         the parsed after program (for fix verification)
     */
    public void categorize(
            Map<String, List<IssueDTO>> beforeIssuesByFinder,
            Set<Issue> afterIssues,
            Program beforeProgram,
            Program afterProgram
    ) {
        Map<String, List<Issue>> afterIssuesByFinder = groupByFinder(afterIssues);

        // Build structural block ID mapping lazily (only when needed and available)
        Map<String, String> blockIdMapping = null;

        // Process each finder from before version
        for (Map.Entry<String, List<IssueDTO>> entry : beforeIssuesByFinder.entrySet()) {
            String finderName = entry.getKey();
            List<IssueDTO> beforeFinderIssues = entry.getValue();
            List<Issue> afterFinderIssues = afterIssuesByFinder.getOrDefault(finderName, List.of());

            blockIdMapping = categorizeFinderIssues(
                    finderName, beforeFinderIssues, afterFinderIssues,
                    beforeProgram, afterProgram, blockIdMapping);
        }

        // Find NEW issues (in after but not matched to any before issue)
        collectNewIssues(afterIssues);
    }

    public List<Issue> getFixed() {
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

    // ========== Main Categorization Logic ==========

    private Map<String, String> categorizeFinderIssues(
            String finderName,
            List<IssueDTO> beforeIssues,
            List<Issue> afterIssues,
            Program beforeProgram,
            Program afterProgram,
            Map<String, String> blockIdMapping
    ) {
        Set<String> afterActorNames = extractActorNames(afterIssues);
        matchedBeforeIssues = new HashSet<>();

        // Pass 1: Match by exact block ID
        matchByExactBlockId(beforeIssues, afterIssues);

        // Pass 2: Match via structural block ID mapping
        blockIdMapping = matchByStructuralMapping(
                beforeIssues, afterIssues, beforeProgram, afterProgram, blockIdMapping);

        // Pass 3: Hint fallback and fix heuristics
        matchByHintFallback(finderName, beforeIssues, afterIssues, afterActorNames, afterProgram);

        return blockIdMapping;
    }

    // ========== Pass 1: Exact Block ID Matching ==========

    /**
     * Matches issues by exact block ID. This is the fast path for programs
     * where block IDs haven't changed.
     */
    private void matchByExactBlockId(List<IssueDTO> beforeIssues, List<Issue> afterIssues) {
        for (IssueDTO beforeIssue : beforeIssues) {
            String blockId = beforeIssue.issueLocationBlockId();

            if (!isBlockIdEmpty(blockId)) {
                Optional<Issue> match = findMatchingIssueByBlockId(
                        afterIssues, beforeIssue.sprite(), blockId);

                match.ifPresent(issue -> markAsPersisting(beforeIssue, issue));
            }
        }
    }

    // ========== Pass 2: Structural Block ID Mapping ==========

    /**
     * For issues that didn't match in Pass 1, tries to match using structural
     * block ID mapping. This handles cases where programs have been re-serialized
     * and block IDs have changed but the structure is the same.
     */
    private Map<String, String> matchByStructuralMapping(
            List<IssueDTO> beforeIssues,
            List<Issue> afterIssues,
            Program beforeProgram,
            Program afterProgram,
            Map<String, String> blockIdMapping
    ) {
        List<IssueDTO> unmatchedWithBlockIds = beforeIssues.stream()
                .filter(i -> !matchedBeforeIssues.contains(i))
                .filter(i -> !isBlockIdEmpty(i.issueLocationBlockId()))
                .toList();

        if (unmatchedWithBlockIds.isEmpty() || beforeProgram == null) {
            return blockIdMapping;
        }

        // Build mapping lazily on first use
        if (blockIdMapping == null) {
            blockIdMapping = BlockIdMapper.buildMapping(beforeProgram, afterProgram);
        }

        for (IssueDTO beforeIssue : unmatchedWithBlockIds) {
            String oldBlockId = beforeIssue.issueLocationBlockId();
            String mappedBlockId = blockIdMapping.get(oldBlockId);

            if (mappedBlockId != null) {
                Optional<Issue> match = findMatchingIssueByBlockId(
                        afterIssues, beforeIssue.sprite(), mappedBlockId);

                match.ifPresent(issue -> markAsPersisting(beforeIssue, issue));
            }
        }

        return blockIdMapping;
    }

    // ========== Pass 3: Hint Fallback ==========

    /**
     * For issues still unmatched after Pass 1 and 2, tries to match by actor + hint.
     * If no match is found, uses fix heuristics to determine if the bug was fixed.
     */
    private void matchByHintFallback(
            String finderName,
            List<IssueDTO> beforeIssues,
            List<Issue> afterIssues,
            Set<String> afterActorNames,
            Program afterProgram
    ) {
        for (IssueDTO beforeIssue : beforeIssues) {
            if (matchedBeforeIssues.contains(beforeIssue)) {
                continue;
            }

            String blockId = beforeIssue.issueLocationBlockId();

            if (isBlockIdEmpty(blockId)) {
                categorizeByActorName(finderName, beforeIssue, afterIssues, afterActorNames, afterProgram);
            } else {
                // Block ID didn't match in Pass 1 or 2, try hint fallback
                Optional<Issue> match = findUnmatchedIssueByActorAndHint(
                        afterIssues, beforeIssue.sprite(), beforeIssue.hint());

                if (match.isPresent()) {
                    markAsPersisting(beforeIssue, match.get());
                } else {
                    categorizeAsFixedOrRemoved(finderName, beforeIssue, afterProgram);
                }
            }
        }
    }

    // ========== Issue Matching Helpers ==========

    private Optional<Issue> findMatchingIssueByBlockId(
            List<Issue> issues, String actorName, String blockId
    ) {
        return issues.stream()
                .filter(i -> !matchedAfterIssues.contains(i))
                .filter(i -> Objects.equals(actorName, i.getActorName()))
                .filter(i -> Objects.equals(blockId, getBlockId(i)))
                .findFirst();
    }

    private Optional<Issue> findUnmatchedIssueByActorAndHint(
            List<Issue> issues, String actorName, String hint
    ) {
        return issues.stream()
                .filter(i -> !matchedAfterIssues.contains(i))
                .filter(i -> Objects.equals(actorName, i.getActorName()))
                .filter(i -> hintsMatch(hint, i.getHint()))
                .findFirst();
    }

    private boolean hintsMatch(String dtoHint, Hint issueHint) {
        String issueHintStr = issueHint != null ? issueHint.getHintText(translator) : null;
        return Objects.equals(dtoHint, issueHintStr);
    }

    private void markAsPersisting(IssueDTO beforeIssue, Issue afterIssue) {
        persisting.add(afterIssue);
        matchedAfterIssues.add(afterIssue);
        matchedBeforeIssues.add(beforeIssue);
    }

    // ========== Categorization Helpers ==========

    private void categorizeByActorName(
            String finderName,
            IssueDTO beforeIssue,
            List<Issue> afterIssues,
            Set<String> afterActorNames,
            Program afterProgram
    ) {
        String actorName = beforeIssue.sprite();

        if (afterActorNames.contains(actorName)) {
            Optional<Issue> match = findUnmatchedIssueByActorAndHint(
                    afterIssues, actorName, beforeIssue.hint());

            if (match.isPresent()) {
                markAsPersisting(beforeIssue, match.get());
            } else {
                categorizeAsFixedOrRemoved(finderName, beforeIssue, afterProgram);
            }
        } else {
            categorizeAsFixedOrRemoved(finderName, beforeIssue, afterProgram);
        }
    }

    private void categorizeAsFixedOrRemoved(
            String finderName, IssueDTO beforeIssue, Program afterProgram
    ) {
        List<FixResult> fixResults = checkIfFixed(beforeIssue, afterProgram);

        if (!fixResults.isEmpty()) {
            List<Issue> fixedIssues = IssueConverter.convertFixResultsToIssues(fixResults, afterProgram);
            fixed.addAll(fixedIssues);
        } else {
            String blockId = beforeIssue.issueLocationBlockId();
            removed.add(new RemovedIssueInfo(
                    finderName,
                    beforeIssue.sprite(),
                    beforeIssue.hint(),
                    isBlockIdEmpty(blockId) ? "N/A" : blockId,
                    beforeIssue.scratchBlocksCode()
            ));
        }
    }

    private List<FixResult> checkIfFixed(IssueDTO beforeIssue, Program program) {
        List<FixHeuristic> applicable = FixHeuristicRegistry.getFixHeuristics(
                beforeIssue.finder(), beforeIssue);

        for (FixHeuristic h : applicable) {
            Set<FixResult> results = h.check(program);
            if (!results.isEmpty()) {
                return new ArrayList<>(results);
            }
        }
        return Collections.emptyList();
    }

    private void collectNewIssues(Set<Issue> afterIssues) {
        for (Issue issue : afterIssues) {
            if (!matchedAfterIssues.contains(issue)) {
                newIssues.add(issue);
            }
        }
    }

    // ========== Utility Methods ==========

    private boolean isBlockIdEmpty(String blockId) {
        return blockId == null || blockId.isEmpty();
    }

    private String getBlockId(Issue issue) {
        ASTNode codeLocation = issue.getCodeLocation();
        return codeLocation != null ? AstNodeUtil.getBlockId(codeLocation) : null;
    }

    private Set<String> extractActorNames(List<Issue> issues) {
        return issues.stream()
                .map(Issue::getActorName)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    /**
     * Groups after issues by finder name with deterministic ordering.
     * Sorting ensures consistent ordering when multiple issues have the same hint.
     */
    private Map<String, List<Issue>> groupByFinder(Set<Issue> issues) {
        return issues.stream()
                .sorted(Comparator
                        .comparing((Issue i) -> i.getActorName() == null ? "" : i.getActorName())
                        .thenComparing(i -> getBlockId(i) == null ? "" : getBlockId(i))
                        .thenComparingInt(Issue::getId))
                .collect(Collectors.groupingBy(Issue::getFinderName));
    }

    /**
     * Info about a removed issue that couldn't be verified as fixed.
     */
    public record RemovedIssueInfo(String finderName, String actorName, String hintText, String blockId,
                                   String codeSnippet) {
    }
}
