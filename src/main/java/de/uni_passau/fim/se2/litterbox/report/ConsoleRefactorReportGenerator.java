/*
 * Copyright (C) 2019-2024 LitterBox contributors
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

import java.util.List;

public class ConsoleRefactorReportGenerator {

    public void generateReport(Program program, List<Refactoring> refactorings) {

        if (refactorings.isEmpty()) {
            System.out.println("No executable refactorings found.");
            return;
        }

        System.out.println("The following refactorings were executed on " + program.getIdent().getName());

        for (int i = 0; i < refactorings.size(); i++) {
            System.out.println(i + ": " + refactorings.get(i).toString());
        }
    }
}
