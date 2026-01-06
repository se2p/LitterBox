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
import de.uni_passau.fim.se2.litterbox.report.IssueDTO;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

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
    private final Scratch3Parser parser;

    /**
     * Creates a new DiffAnalyzer.
     *
     * @param detectors         comma-separated list of detector names or groups
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
     * @param afterPath  path to the after version (.sb3 only)
     * @return categorized diff results
     * @throws IOException      if files cannot be read
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

        // Categorize issues
        IssueCategorizer categorizer = new IssueCategorizer();
        categorizer.categorize(beforeIssuesByFinder, afterIssues, afterProgram);

        // Convert results to Issue objects for consistent API
        List<Issue> fixedIssues = IssueConverter.convertFixResultsToIssues(
                categorizer.getFixed(), afterProgram);
        List<Issue> removedIssues = IssueConverter.createRemovedIssues(
                categorizer.getRemoved(), afterProgram);

        return new DiffResult(
                beforeSource,
                afterSource,
                fixedIssues,
                categorizer.getNewIssues(),
                categorizer.getPersisting(),
                removedIssues
        );
    }

    private Map<String, List<IssueDTO>> loadBeforeIssues(Path beforePath)
            throws IOException, ParsingException {
        if (isJsonFile(beforePath)) {
            return loadIssuesFromJson(beforePath);
        } else {
            return analyzeAndConvertToDTO(beforePath);
        }
    }

    private Set<Issue> analyzeProgram(Program program) {
        ProgramBugAnalyzer analyzer = new ProgramBugAnalyzer(detectors, ignoreLooseBlocks);
        return analyzer.analyze(program);
    }

    private Map<String, List<IssueDTO>> analyzeAndConvertToDTO(Path projectPath)
            throws ParsingException {
        Program program = parser.parseFile(projectPath.toFile());
        Set<Issue> issues = analyzeProgram(program);

        Map<String, List<IssueDTO>> result = new HashMap<>();
        for (Issue issue : issues) {
            String finderName = issue.getFinderName();
            IssueDTO dto = IssueConverter.toMinimalDTO(issue);
            result.computeIfAbsent(finderName, k -> new ArrayList<>()).add(dto);
        }
        return result;
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
}
