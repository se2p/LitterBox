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
import java.util.Collection;

public class JSONReportGenerator implements ReportGenerator {

    @Override
    public void generateReport(Program program, Collection<Issue> issues) throws IOException {
        // TODO: Implement putting all this information in a JSON file 
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
            ScratchBlocksVisitor blockVisitor = new ScratchBlocksVisitor(System.out);
            // location.accept(blockVisitor); // TODO: Implement
            String scratchBlockCode = blockVisitor.getScratchBlocks();
            builder.append(scratchBlockCode);

            builder.append(System.lineSeparator());
        }
    }
}
