/*
 * Copyright (C) 2019-2021 LitterBox contributors
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

import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.Refactoring;

import java.io.IOException;
import java.util.List;

public class ConsoleRefactorReportGenerator {

    // TODO get List of concrete refactorings here
    public void generateReport(Program program, List<Integer> productions) throws IOException {

        if (productions.isEmpty()) {
            System.out.println("No executable refactorings found.");
            return;
        }

        System.out.println("The following refactorings were executed on " + program.getIdent().getName());

        for (int i = 0; i < productions.size(); i++) {
            Integer production = productions.get(i);
            System.out.println(i + ": " + production);
        }
    }
}
