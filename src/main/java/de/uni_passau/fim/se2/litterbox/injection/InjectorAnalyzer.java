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
package de.uni_passau.fim.se2.litterbox.injection;

import de.uni_passau.fim.se2.litterbox.analytics.FileAnalyzer;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.ProgramAnalyzer;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.jsoncreation.JSONFileCreator;
import de.uni_passau.fim.se2.litterbox.report.CSVPrinterFactory;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslator;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Analyzer that runs BugInjectors on Scratch programs and saves the mutated versions.
 */
public class InjectorAnalyzer extends FileAnalyzer<Map<BugInjector, List<InjectionResult>>> {

    private static final Logger log = Logger.getLogger(InjectorAnalyzer.class.getName());

    private static final List<String> CSV_HEADERS = List.of(
            "original_project",
            "bug_finder",
            "mutated_project",
            "sprite_name",
            "original_script_id",
            "mutated_script_id",
            "original_bug_count",
            "mutated_bug_count",
            "hint",
            "original_script",
            "mutated_script"
    );

    private final Path csvPath;
    private final IssueTranslator translator;
    private CSVPrinter csvPrinter;

    public InjectorAnalyzer(Path output, boolean delete, List<BugInjector> injectors) {
        this(output, delete, injectors, null, false, null);
    }

    public InjectorAnalyzer(Path output, boolean delete, List<BugInjector> injectors, Path csvPath) {
        this(output, delete, injectors, csvPath, false, null);
    }

    public InjectorAnalyzer(
            Path output, boolean delete, List<BugInjector> injectors, Path csvPath, boolean skipAnalyzed) {
        this(output, delete, injectors, csvPath, skipAnalyzed, null);
    }

    public InjectorAnalyzer(
            Path output,
            boolean delete,
            List<BugInjector> injectors,
            Path csvPath,
            boolean skipAnalyzed,
            IssueTranslator translator) {
        super(new InjectorProgramAnalyzer(injectors), output, delete, skipAnalyzed);
        this.csvPath = csvPath;
        this.translator = translator;
    }

    @Override
    protected void beginAnalysis() throws IOException {
        super.beginAnalysis();
        if (csvPath != null) {
            csvPrinter = CSVPrinterFactory.getNewPrinter(csvPath, CSV_HEADERS);
        }
    }

    @Override
    protected void endAnalysis() throws IOException {
        if (csvPrinter != null) {
            csvPrinter.close();
        }
        super.endAnalysis();
    }

    @Override
    protected void writeResultToFile(
            Path projectFile, Program program, Map<BugInjector, List<InjectionResult>> result
    ) throws IOException {
        String originalExt = FilenameUtils.getExtension(projectFile.toString());
        String originalProjectName = program.getIdent().getName();

        int totalInjected = 0;
        for (Map.Entry<BugInjector, List<InjectionResult>> entry : result.entrySet()) {
            BugInjector injector = entry.getKey();
            List<InjectionResult> injectionResults = entry.getValue();

            // Cache original issues - we need both count and the issues for hint comparison
            Set<Issue> originalIssues = null;
            if (csvPrinter != null) {
                originalIssues = injector.getIssueFinder().check(program);
            }

            int index = 0;
            for (InjectionResult injectionResult : injectionResults) {
                Program mutated = injectionResult.mutatedProgram();
                // Suffix: _BugName_Index
                String suffix = "_" + injector.getName() + "_" + index;
                String mutatedProjectName = originalProjectName + suffix;

                if (originalExt.equalsIgnoreCase("json")) {
                    JSONFileCreator.writeJsonFromProgram(mutated, output, suffix);
                } else {
                    JSONFileCreator.writeSb3FromProgram(mutated, output, projectFile.toFile(), suffix);
                }

                // Write to CSV if enabled
                if (csvPrinter != null) {
                    writeCSVRow(program, mutated, injector, mutatedProjectName, injectionResult, originalIssues);
                }

                index++;
                totalInjected++;
            }
        }
        log.info("Injected " + totalInjected + " bugs into " + projectFile.getFileName());
    }

    private void writeCSVRow(
            Program original,
            Program mutated,
            BugInjector injector,
            String mutatedProjectName,
            InjectionResult injectionResult,
            Set<Issue> originalIssues
    ) throws IOException {
        IssueFinder finder = injector.getIssueFinder();
        if (finder == null) {
            log.warning("Injector " + injector.getName() + " returned null IssueFinder, skipping CSV row");
            return;
        }
        String originalProjectName = original.getIdent().getName();

        // Get mutated issues
        Set<Issue> mutatedIssues = finder.check(mutated);

        // Find the hint for NEW issues only (issues that don't exist in the original)
        // Use mutatedScriptId for hint matching since that's the ID in the mutated issue's AST
        String hintText = getHintForNewIssue(originalIssues, mutatedIssues, injectionResult.mutatedScriptId());

        csvPrinter.printRecord(
                originalProjectName,
                finder.getName(),
                mutatedProjectName,
                injectionResult.spriteName(),
                injectionResult.originalScriptId(),
                injectionResult.mutatedScriptId(),
                originalIssues.size(),
                mutatedIssues.size(),
                hintText,
                injectionResult.originalScriptScratchBlocks(),
                injectionResult.mutatedScriptScratchBlocks()
        );
        csvPrinter.flush();
    }

    /**
     * Finds the hint text for a NEW issue (one that exists in mutated but not in original).
     * Prioritizes issues in the target script, then falls back to any new issue.
     */
    private String getHintForNewIssue(Set<Issue> originalIssues, Set<Issue> mutatedIssues, String scriptId) {
        if (mutatedIssues.isEmpty()) {
            return "";
        }

        // Collect hint texts from original issues for comparison
        Set<String> originalHints = new HashSet<>();
        for (Issue issue : originalIssues) {
            originalHints.add(getHintText(issue));
        }

        // Find NEW issues (hints that don't exist in original)
        List<Issue> newIssues = new ArrayList<>();
        for (Issue issue : mutatedIssues) {
            String hint = getHintText(issue);
            if (!hint.isEmpty() && !originalHints.contains(hint)) {
                newIssues.add(issue);
            }
        }

        if (newIssues.isEmpty()) {
            // No truly new issues found - fall back to any issue in target script
            return getHintForScript(mutatedIssues, scriptId);
        }

        // Prefer new issue in the target script
        if (scriptId != null && !scriptId.isEmpty()) {
            for (Issue issue : newIssues) {
                if (issue.getScriptOrProcedureDefinition() != null) {
                    String issueScriptId = AstNodeUtil.getBlockId(issue.getScriptOrProcedureDefinition());
                    if (scriptId.equals(issueScriptId)) {
                        return getHintText(issue);
                    }
                }
            }
        }

        // Return first new issue's hint
        return getHintText(newIssues.getFirst());
    }

    /**
     * Fallback: Finds the hint text for the issue whose script matches the given scriptId.
     */
    private String getHintForScript(Set<Issue> issues, String scriptId) {
        if (issues.isEmpty()) {
            return "";
        }

        // Try to find issue matching the scriptId
        if (scriptId != null && !scriptId.isEmpty()) {
            for (Issue issue : issues) {
                if (issue.getScriptOrProcedureDefinition() != null) {
                    String issueScriptId = AstNodeUtil.getBlockId(issue.getScriptOrProcedureDefinition());
                    if (scriptId.equals(issueScriptId)) {
                        return getHintText(issue);
                    }
                }
            }
        }

        // Fallback to first issue
        return getHintText(issues.iterator().next());
    }

    private String getHintText(Issue issue) {
        if (translator != null && issue.getHint() != null) {
            return issue.getHintText(translator);
        } else if (issue.getHint() != null) {
            // No translator - return the hint key as fallback
            return issue.getFinder().getName();
        }
        return "";
    }

    private record InjectorProgramAnalyzer(
            List<BugInjector> injectors) implements ProgramAnalyzer<Map<BugInjector, List<InjectionResult>>> {

        @Override
        public Map<BugInjector, List<InjectionResult>> analyze(Program program) {
            Map<BugInjector, List<InjectionResult>> result = new LinkedHashMap<>();
            for (BugInjector injector : injectors) {
                log.info("Applying injection for " + injector);
                try {
                    // Injector is responsible for cloning the program as needed.
                    List<InjectionResult> injected = injector.inject(program);
                    if (!injected.isEmpty()) {
                        result.put(injector, injected);
                    }
                } catch (RuntimeException e) {
                    log.log(Level.SEVERE, "Injector " + injector.getName()
                            + " failed on project " + program.getIdent().getName(), e);
                }
            }
            return result;
        }
    }
}
