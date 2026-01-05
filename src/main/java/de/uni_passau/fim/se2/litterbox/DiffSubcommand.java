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
import picocli.CommandLine;

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
            description = "Path to the prior version (.sb3) or prior analysis report (.json).",
            required = true
    )
    Path beforePath;

    @CommandLine.Option(
            names = {"-a", "--after"},
            description = "Path to the modified/new project file (.sb3).",
            required = true
    )
    Path afterPath;

    @CommandLine.Option(
            names = {"-o", "--output"},
            description = "Optional: Path for output file (JSON format)."
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
            names = {"--summary-only"},
            description = "Only print summary, no details."
    )
    boolean summaryOnly;

    @Override
    public Integer call() throws Exception {
        validateParams();

        DiffAnalyzer analyzer = new DiffAnalyzer(detectors, ignoreLooseBlocks);
        DiffResult result = analyzer.analyze(beforePath, afterPath);

        if (outputPath != null) {
            writeJsonOutput(result);
        } else {
            writeConsoleOutput(result);
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
        if (!afterPath.toString().toLowerCase().endsWith(".sb3")) {
            throw new CommandLine.ParameterException(
                    spec.commandLine(),
                    "After file must be a .sb3 file: " + afterPath
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

    private void writeConsoleOutput(DiffResult result) {
        PrintWriter out = spec.commandLine().getOut();

        out.println("=== BUG DIFF REPORT ===");
        out.printf("Comparing: %s → %s%n%n", result.beforeSource(), result.afterSource());

        out.println("SUMMARY:");
        out.printf("  FIXED:      %d%n", result.fixedCount());
        out.printf("  NEW:        %d%n", result.newCount());
        out.printf("  PERSISTING: %d%n", result.persistingCount());
        out.printf("  REMOVED:    %d%n%n", result.removedCount());

        if (!summaryOnly) {
            printIssueList(out, "FIXED BUGS", "✓", result.fixed());
            printIssueList(out, "NEW BUGS", "✗", result.newIssues());
            printIssueList(out, "PERSISTING BUGS", "-", result.persisting());
            printIssueList(out, "REMOVED (unverified)", "?", result.removed());
        }
    }

    private void printIssueList(PrintWriter out, String header, String marker, List<Issue> issues) {
        if (issues.isEmpty()) {
            return;
        }

        out.println(header + ":");
        for (Issue issue : issues) {
            ASTNode codeLocation = issue.getCodeLocation();
            String blockId = codeLocation != null ? AstNodeUtil.getBlockId(codeLocation) : null;
            out.printf("  %s %s in %s (block: %s)%n",
                    marker,
                    issue.getFinderName(),
                    issue.getActorName(),
                    blockId != null ? blockId : "unknown"
            );
        }
        out.println();
    }

    private void writeJsonOutput(DiffResult result) throws IOException {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append(String.format("  \"beforeProject\": \"%s\",%n", escapeJson(result.beforeSource())));
        json.append(String.format("  \"afterProject\": \"%s\",%n", escapeJson(result.afterSource())));
        json.append("  \"summary\": {\n");
        json.append(String.format("    \"fixed\": %d,%n", result.fixedCount()));
        json.append(String.format("    \"new\": %d,%n", result.newCount()));
        json.append(String.format("    \"persisting\": %d,%n", result.persistingCount()));
        json.append(String.format("    \"removed\": %d%n", result.removedCount()));
        json.append("  },\n");
        json.append("  \"fixed\": ").append(issuesToJsonArray(result.fixed())).append(",\n");
        json.append("  \"new\": ").append(issuesToJsonArray(result.newIssues())).append(",\n");
        json.append("  \"persisting\": ").append(issuesToJsonArray(result.persisting())).append(",\n");
        json.append("  \"removed\": ").append(issuesToJsonArray(result.removed())).append("\n");
        json.append("}\n");

        Files.writeString(outputPath, json.toString());
        spec.commandLine().getOut().println("Diff report written to: " + outputPath);
    }

    private String issuesToJsonArray(List<Issue> issues) {
        if (issues.isEmpty()) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder("[\n");
        for (int i = 0; i < issues.size(); i++) {
            Issue issue = issues.get(i);
            ASTNode codeLocation = issue.getCodeLocation();
            String blockId = codeLocation != null ? AstNodeUtil.getBlockId(codeLocation) : null;
            sb.append("    {\n");
            sb.append(String.format("      \"finder\": \"%s\",%n", escapeJson(issue.getFinderName())));
            sb.append(String.format("      \"actor\": \"%s\",%n", escapeJson(issue.getActorName())));
            sb.append(String.format("      \"blockId\": %s%n",
                    blockId != null ? "\"" + escapeJson(blockId) + "\"" : "null"));
            sb.append("    }");
            if (i < issues.size() - 1) {
                sb.append(",");
            }
            sb.append("\n");
        }
        sb.append("  ]");
        return sb.toString();
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
