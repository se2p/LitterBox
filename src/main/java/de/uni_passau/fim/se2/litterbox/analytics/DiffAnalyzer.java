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

import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.parser.Scratch3Parser;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.diff.CodeDiffGenerator;
import de.uni_passau.fim.se2.litterbox.diff.ProjectCodeDiff;
import de.uni_passau.fim.se2.litterbox.report.IssueDTO;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Analyzes differences between two versions of a Scratch project.
 *
 * <p>Compares issues found in the "before" version with issues in the "after" version,
 * categorizing them into FIXED, NEW, PERSISTING, and REMOVED.
 *
 * <p>This class coordinates the diff analysis by delegating to:
 * <ul>
 *   <li>{@link IssueCategorizer} - categorizes issues between versions</li>
 *   <li>{@link IssueConverter} - converts between issue representations</li>
 *   <li>{@link ProgramBugAnalyzer} - analyzes programs for bugs</li>
 * </ul>
 */
public class DiffAnalyzer {

    private final String detectors;
    private final boolean ignoreLooseBlocks;
    private final IssueTranslator translator;

    /**
     * Creates a new DiffAnalyzer.
     *
     * @param detectors         comma-separated list of detector names or groups
     * @param ignoreLooseBlocks whether to ignore loose blocks during analysis
     * @param translator        the translator to use for hint text generation
     */
    public DiffAnalyzer(String detectors, boolean ignoreLooseBlocks, IssueTranslator translator) {
        this.detectors = detectors;
        this.ignoreLooseBlocks = ignoreLooseBlocks;
        this.translator = translator;
    }

    /**
     * Analyzes the diff between two project versions.
     *
     * @param beforePath path to the before version (.sb3, project.json, or LitterBox report .json)
     * @param afterPath  path to the after version (.sb3 or project.json)
     * @return categorized diff results
     * @throws IOException      if files cannot be read
     * @throws ParsingException if files cannot be parsed
     */
    public DiffResult analyze(Path beforePath, Path afterPath) throws IOException, ParsingException {
        Program afterProgram = parseProject(afterPath);

        BeforeData beforeData = loadBeforeData(beforePath);
        Set<Issue> afterIssues = analyzeProgram(afterProgram);

        IssueCategorizer categorizer = new IssueCategorizer(translator);
        categorizer.categorize(beforeData.issues(), afterIssues, beforeData.program(), afterProgram);

        List<Issue> removedIssues = IssueConverter.createRemovedIssues(
                categorizer.getRemoved(), afterProgram);

        ProjectCodeDiff projectCodeDiff;
        if (beforeData.program() != null) {
            CodeDiffGenerator codeDiffGenerator = new CodeDiffGenerator();
            projectCodeDiff = codeDiffGenerator.generate(beforeData.program(), afterProgram);
        } else {
            projectCodeDiff = ProjectCodeDiff.empty();
        }

        return new DiffResult(
                beforePath.toString(),
                afterPath.toString(),
                categorizer.getFixed(),
                categorizer.getNewIssues(),
                categorizer.getPersisting(),
                removedIssues,
                projectCodeDiff
        );
    }

    // ========== Before Data Loading ==========

    /**
     * Data loaded from the before source: issues and optionally the program.
     *
     * @param issues  issues from the before version, grouped by finder name
     * @param program the parsed program (null if loaded from LitterBox JSON report)
     */
    private record BeforeData(Map<String, List<IssueDTO>> issues, Program program) {
    }

    private BeforeData loadBeforeData(Path beforePath) throws IOException, ParsingException {
        if (isJsonFile(beforePath)) {
            if (isLitterBoxReport(beforePath)) {
                // LitterBox JSON report has no program for structural mapping
                return new BeforeData(loadIssuesFromJson(beforePath), null);
            } else {
                // project.json file
                return analyzeAndReturnBoth(beforePath);
            }
        } else {
            // .sb3 file
            return analyzeAndReturnBoth(beforePath);
        }
    }

    private BeforeData analyzeAndReturnBoth(Path projectPath) throws ParsingException {
        Program program = parseProject(projectPath);
        Set<Issue> issues = analyzeProgram(program);

        Map<String, List<IssueDTO>> issuesByFinder = convertToSortedDTOs(issues);
        return new BeforeData(issuesByFinder, program);
    }

    // ========== Program Analysis ==========

    private Program parseProject(Path projectPath) throws ParsingException {
        Scratch3Parser parser = new Scratch3Parser();
        return parser.parseFile(projectPath.toFile());
    }

    private Set<Issue> analyzeProgram(Program program) {
        ProgramBugAnalyzer analyzer = new ProgramBugAnalyzer(detectors, ignoreLooseBlocks);
        return analyzer.analyze(program);
    }

    // ========== Issue Conversion ==========

    /**
     * Converts issues to DTOs with deterministic ordering.
     *
     * <p>Sorting BEFORE issues complements {@link IssueCategorizer} which
     * sorts AFTER issues. In Pass 3 (hint fallback), when multiple issues share the
     * same actor + hint, iteration order determines matching priority.
     */
    private Map<String, List<IssueDTO>> convertToSortedDTOs(Set<Issue> issues) {
        List<Issue> sortedIssues = issues.stream()
                .sorted(createIssueComparator())
                .toList();

        Map<String, List<IssueDTO>> result = new HashMap<>();
        for (Issue issue : sortedIssues) {
            String finderName = issue.getFinderName();
            result.computeIfAbsent(finderName, k -> new ArrayList<>())
                    .add(IssueConverter.toMinimalDTO(issue, translator));
        }
        return result;
    }

    private Comparator<Issue> createIssueComparator() {
        return Comparator
                .comparing((Issue i) -> i.getActorName() == null ? "" : i.getActorName())
                .thenComparing(i -> {
                    var loc = i.getCodeLocation();
                    return loc != null ? AstNodeUtil.getBlockId(loc) : "";
                }, Comparator.nullsFirst(Comparator.naturalOrder()))
                .thenComparingInt(Issue::getId);
    }

    // ========== JSON Loading ==========

    private Map<String, List<IssueDTO>> loadIssuesFromJson(Path jsonPath)
            throws IOException, ParsingException {
        File file = jsonPath.toFile();
        IssueParser issueParser = new IssueParser();
        Map<String, List<IssueDTO>> allIssues = issueParser.getIssuesPerFinder(file);

        // Filter to only include issues from the selected detectors
        Set<String> selectedFinders = getSelectedFinderNames();
        return allIssues.entrySet().stream()
                .filter(entry -> selectedFinders.contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Set<String> getSelectedFinderNames() {
        return IssueTool.getFinders(detectors).stream()
                .map(IssueFinder::getName)
                .collect(Collectors.toSet());
    }

    // ========== File Type Detection ==========

    private boolean isJsonFile(Path path) {
        return path.toString().toLowerCase().endsWith(".json");
    }

    /**
     * Detects whether a JSON file is a LitterBox report or a project.json.
     *
     * <p>project.json files have a "targets" field; LitterBox reports do not.
     */
    private boolean isLitterBoxReport(Path path) throws IOException {
        String content = Files.readString(path);
        return !content.contains("\"targets\"");
    }
}
