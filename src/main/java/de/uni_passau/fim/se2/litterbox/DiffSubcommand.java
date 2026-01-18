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
package de.uni_passau.fim.se2.litterbox;

import de.uni_passau.fim.se2.litterbox.analytics.DiffAnalyzer;
import de.uni_passau.fim.se2.litterbox.analytics.DiffResult;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.export.scratchblocks.ScratchBlocksVisitor;
import de.uni_passau.fim.se2.litterbox.report.CSVPrinterFactory;
import de.uni_passau.fim.se2.litterbox.report.HTMLDiffReportGenerator;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslator;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslatorFactory;
import org.apache.commons.csv.CSVPrinter;
import picocli.CommandLine;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * CLI subcommand for comparing two versions of a Scratch project.
 *
 * <p>Analyzes differences between a "before" and "after" version, categorizing
 * issues as FIXED, NEW, PERSISTING, or REMOVED.
 *
 * <p>Example usage:
 * <pre>
 * litterbox diff --before game_v1.sb3 --after game_v2.sb3
 * litterbox diff --before prior_report.json --after game_v2.sb3 --output diff.json
 * </pre>
 */
@CommandLine.Command(
        name = "diff",
        description = "Compare two versions of a Scratch project and report bug changes.",
        mixinStandardHelpOptions = true
)
public class DiffSubcommand implements Callable<Integer> {

    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    @CommandLine.Option(
            names = {"-b", "--before"},
            description = "Path to the prior version (.sb3, project.json, or LitterBox report .json).",
            required = true
    )
    Path beforePath;

    @CommandLine.Option(
            names = {"-a", "--after"},
            description = "Path to the modified/new project file (.sb3 or project.json).",
            required = true
    )
    Path afterPath;

    @CommandLine.Option(
            names = {"-o", "--output"},
            description = "Optional: Path for output file. Format determined by extension (.json or .csv)."
    )
    Path outputPath;

    @CommandLine.Option(
            names = {"-d", "--detectors"},
            description = "Detectors to use, separated by commas.",
            defaultValue = "bugs"
    )
    String detectors;

    @CommandLine.Option(
            names = {"-i", "--ignore-loose"},
            description = "Ignore loose blocks when checking bug patterns."
    )
    boolean ignoreLooseBlocks;

    @CommandLine.Option(
            names = {"-l", "--lang"},
            description = "Language of the hints in the output.",
            defaultValue = "en"
    )
    String language = "en";

    @Override
    public Integer call() throws Exception {
        validateParams();

        IssueTranslator translator = IssueTranslatorFactory.getIssueTranslator(java.util.Locale.of(language));
        DiffAnalyzer analyzer = new DiffAnalyzer(detectors, ignoreLooseBlocks, translator);
        DiffResult result = analyzer.analyze(beforePath, afterPath);

        if (outputPath == null) {
            writeConsoleOutput(result, translator);
        } else if (outputPath.getFileName().toString().endsWith(".json")) {
            writeJsonOutput(result);
        } else if (outputPath.getFileName().toString().endsWith(".csv")) {
            writeCsvOutput(result);
        } else if (outputPath.getFileName().toString().endsWith(".html")
                || outputPath.getFileName().toString().endsWith(".htm")) {
            writeHtmlOutput(result);
        } else {
            throw new IllegalArgumentException("Unknown output file type: " + outputPath
                    + ". Supported extensions: .json, .csv, .html");
        }

        return 0;
    }

    private void validateParams() throws CommandLine.ParameterException {
        if (!Files.exists(beforePath)) {
            throw new CommandLine.ParameterException(
                    spec.commandLine(),
                    "Before file does not exist: " + beforePath
            );
        }
        if (!Files.exists(afterPath)) {
            throw new CommandLine.ParameterException(
                    spec.commandLine(),
                    "After file does not exist: " + afterPath
            );
        }
        String afterLower = afterPath.toString().toLowerCase();
        if (!afterLower.endsWith(".sb3") && !afterLower.endsWith(".json")) {
            throw new CommandLine.ParameterException(
                    spec.commandLine(),
                    "After file must be a .sb3 or .json file: " + afterPath
            );
        }
        String beforeLower = beforePath.toString().toLowerCase();
        if (!beforeLower.endsWith(".sb3") && !beforeLower.endsWith(".json")) {
            throw new CommandLine.ParameterException(
                    spec.commandLine(),
                    "Before file must be a .sb3 or .json file: " + beforePath
            );
        }
    }

    private void writeConsoleOutput(DiffResult result, IssueTranslator translator) {
        PrintWriter out = spec.commandLine().getOut();

        out.println("=== BUG DIFF REPORT ===");
        out.printf("Comparing: %s → %s%n%n", result.beforeSource(), result.afterSource());

        out.println("SUMMARY:");
        out.printf("  FIXED:      %d%n", result.fixedCount());
        out.printf("  NEW:        %d%n", result.newCount());
        out.printf("  PERSISTING: %d%n", result.persistingCount());
        out.printf("  REMOVED:    %d%n%n", result.removedCount());

        printIssueList(out, "FIXED BUGS", "✓", result.fixed(), translator);
        printIssueList(out, "NEW BUGS", "✗", result.newIssues(), translator);
        printIssueList(out, "PERSISTING BUGS", "-", result.persisting(), translator);
        printIssueList(out, "REMOVED (unverified)", "?", result.removed(), translator);
    }

    private void printIssueList(PrintWriter out, String header, String marker, List<Issue> issues, IssueTranslator translator) {
        if (issues.isEmpty()) {
            return;
        }

        out.println(header + ":");
        for (Issue issue : issues) {
            String codeSnippet = getCodeSnippet(issue);
            out.printf("  %s %s in %s: %s%n",
                    marker,
                    issue.getTranslatedFinderName(translator),
                    issue.getActorName(),
                    codeSnippet
            );
        }
        out.println();
    }

    private String getCodeSnippet(Issue issue) {
        ASTNode codeLocation = issue.getCodeLocation();
        if (codeLocation != null) {
            // Convert to scratchblocks notation and take first line only
            String scratchBlocks = ScratchBlocksVisitor.of(codeLocation).trim();
            int newlineIndex = scratchBlocks.indexOf('\n');
            if (newlineIndex > 0) {
                scratchBlocks = scratchBlocks.substring(0, newlineIndex);
            }
            return scratchBlocks;
        }

        // For fixed issues where the node was deleted, use the stored code snippet
        String originalCode = issue.getOriginalCodeSnippet();
        if (originalCode != null && !originalCode.isBlank()) {
            // Take first line only for compact display
            return originalCode.lines().findFirst().orElse(originalCode).trim();
        }

        // Final fallback to block ID
        String blockId = issue.getOriginalBlockId();
        return blockId != null ? "(block: " + blockId + ")" : "(unknown location)";
    }

    private void writeJsonOutput(DiffResult result) {
        DiffReportJson report = new DiffReportJson(
                result.beforeSource(),
                result.afterSource(),
                new SummaryJson(
                        result.fixedCount(),
                        result.newCount(),
                        result.persistingCount(),
                        result.removedCount()
                ),
                issuesToJson(result.fixed()),
                issuesToJson(result.newIssues()),
                issuesToJson(result.persisting()),
                issuesToJson(result.removed())
        );

        JsonMapper mapper = JsonMapper.builder()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .build();
        mapper.writeValue(outputPath.toFile(), report);

        spec.commandLine().getOut().println("Diff report written to: " + outputPath);
    }

    private void writeCsvOutput(DiffResult result) throws IOException {
        List<String> headers = List.of(
                "beforeProject", "afterProject", "category", "finder", "actor", "blockId"
        );

        try (CSVPrinter printer = CSVPrinterFactory.getNewPrinter(outputPath, headers)) {
            String beforeProject = result.beforeSource();
            String afterProject = result.afterSource();

            writeIssuesToCsv(printer, beforeProject, afterProject, "FIXED", result.fixed());
            writeIssuesToCsv(printer, beforeProject, afterProject, "NEW", result.newIssues());
            writeIssuesToCsv(printer, beforeProject, afterProject, "PERSISTING", result.persisting());
            writeIssuesToCsv(printer, beforeProject, afterProject, "REMOVED", result.removed());

            printer.flush();
        }

        spec.commandLine().getOut().println("Diff report written to: " + outputPath);
    }

    private void writeHtmlOutput(DiffResult result) throws IOException {
        IssueTranslator translator = IssueTranslatorFactory.getIssueTranslator(java.util.Locale.of(language));
        HTMLDiffReportGenerator generator = new HTMLDiffReportGenerator(translator, outputPath);
        generator.generateReport(result);
        spec.commandLine().getOut().println("HTML diff report written to: " + outputPath);
    }

    private void writeIssuesToCsv(
            CSVPrinter printer,
            String beforeProject,
            String afterProject,
            String category,
            List<Issue> issues
    ) throws IOException {
        for (Issue issue : issues) {
            ASTNode codeLocation = issue.getCodeLocation();
            String blockId = codeLocation != null ? AstNodeUtil.getBlockId(codeLocation) : null;
            if (blockId == null) {
                blockId = issue.getOriginalBlockId();
            }
            printer.printRecord(
                    beforeProject,
                    afterProject,
                    category,
                    issue.getFinderName(),
                    issue.getActorName(),
                    blockId != null ? blockId : ""
            );
        }
    }

    private List<IssueJson> issuesToJson(List<Issue> issues) {
        return issues.stream()
                .map(issue -> {
                    ASTNode codeLocation = issue.getCodeLocation();
                    String blockId = codeLocation != null ? AstNodeUtil.getBlockId(codeLocation) : null;
                    if (blockId == null) {
                        blockId = issue.getOriginalBlockId();
                    }
                    return new IssueJson(issue.getFinderName(), issue.getActorName(), blockId);
                })
                .toList();
    }

    // JSON DTOs for Jackson serialization
    private record DiffReportJson(
            String beforeProject,
            String afterProject,
            SummaryJson summary,
            List<IssueJson> fixed,
            List<IssueJson> newIssues,
            List<IssueJson> persisting,
            List<IssueJson> removed
    ) {
    }

    private record SummaryJson(
            int fixed,
            int newBugs,
            int persisting,
            int removed
    ) {
    }

    private record IssueJson(String finder, String actor, String blockId) {
    }
}
