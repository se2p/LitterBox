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
package de.uni_passau.fim.se2.litterbox.report;

import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;

import java.io.IOException;
import java.util.*;

public class ConsoleReportGenerator implements ReportGenerator {

    private final List<String> detectors;

    public ConsoleReportGenerator(List<String> detectors) {
        this.detectors = new ArrayList<>(detectors);
    }

    @Override
    public void generateReport(Program program, Collection<Issue> issues) throws IOException {

        if (issues.isEmpty()) {
            System.out.println("No issues found.");
            return;
        }

        // Group issues by IssueType
        Map<IssueType, List<Issue>> issuesByType = new EnumMap<>(IssueType.class);
        for (Issue issue : issues) {
            issuesByType.computeIfAbsent(issue.getIssueType(), k -> new ArrayList<>()).add(issue);
        }

        // Print header
        System.out.println("=== CHECK REPORT ===");
        System.out.println("Project: " + program.getIdent().getName());
        System.out.println();

        // Print summary (only categories with issues)
        System.out.println("SUMMARY:");
        printSummaryLine("BUGS", issuesByType.get(IssueType.BUG));
        printSummaryLine("SMELLS", issuesByType.get(IssueType.SMELL));
        printSummaryLine("PERFUMES", issuesByType.get(IssueType.PERFUME));
        System.out.println();

        // Print detailed lists by category
        printCategoryDetails("BUGS", IssueType.BUG, issuesByType);
        printCategoryDetails("SMELLS", IssueType.SMELL, issuesByType);
        printCategoryDetails("PERFUMES", IssueType.PERFUME, issuesByType);
    }

    private void printSummaryLine(String label, List<Issue> issues) {
        if (issues != null && !issues.isEmpty()) {
            System.out.printf("  %-10s %d%n", label + ":", issues.size());
        }
    }

    private void printCategoryDetails(String header, IssueType type, Map<IssueType, List<Issue>> issuesByType) {
        List<Issue> categoryIssues = issuesByType.get(type);
        if (categoryIssues == null || categoryIssues.isEmpty()) {
            return;
        }

        System.out.println(header + ":");

        // Group by finder name to show counts
        Map<String, List<Issue>> byFinder = new LinkedHashMap<>();
        for (String detector : detectors) {
            for (Issue issue : categoryIssues) {
                if (issue.getFinderName().equals(detector)) {
                    byFinder.computeIfAbsent(detector, k -> new ArrayList<>()).add(issue);
                }
            }
        }

        for (Map.Entry<String, List<Issue>> entry : byFinder.entrySet()) {
            int count = entry.getValue().size();
            String plural = count == 1 ? "" : "s";
            System.out.printf("  • %s (%d occurrence%s)%n", entry.getKey(), count, plural);
        }
        System.out.println();
    }
}
