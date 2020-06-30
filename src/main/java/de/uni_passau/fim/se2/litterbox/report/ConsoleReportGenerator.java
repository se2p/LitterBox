/*
 * Copyright (C) 2020 LitterBox contributors
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
import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class ConsoleReportGenerator implements ReportGenerator {

    private List<String> detectors;

    public ConsoleReportGenerator(String[] detectors) {
        this.detectors = Arrays.asList(detectors);
    }

    @Override
    public void generateReport(Program program, Collection<Issue> issues) throws IOException {
        StringBuilder builder = new StringBuilder();
        for(Issue issue : issues) {
            builder.append(issue.getFinderName());
            builder.append(": ");
            builder.append(System.lineSeparator());

            builder.append(issue.getHint());
            builder.append(System.lineSeparator());

            builder.append("  Actor: ");
            builder.append(issue.getActorName());
            builder.append(System.lineSeparator());

            builder.append("  Script: ");
            AbstractNode location = issue.getCodeLocation();
            ScratchBlocksVisitor blockVisitor = new ScratchBlocksVisitor();
            // location.accept(blockVisitor); // TODO: Implement
            String scratchBlockCode = blockVisitor.getScratchBlocks();
            builder.append(scratchBlockCode);

            builder.append(System.lineSeparator());
        }

        String result = builder.toString();
        if(result.isEmpty()) {
            System.out.println("No issues found in project.");
        } else {
            System.out.println(result);
        }
    }
}
