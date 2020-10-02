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

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.bugpattern.EndlessRecursion;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.google.common.truth.Truth.assertThat;

public class CSVReportGeneratorTest implements JsonTest {

    @Test
    public void testSingleIssueSingleProjectNewCSV() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/bugpattern/recursiveProcedure.json");
        EndlessRecursion finder = new EndlessRecursion();
        Set<Issue> issues = finder.check(program);

        Path tmpFile = Files.createTempFile("foo", "bar");
        List<String> finders = new ArrayList<>();
        finders.add(EndlessRecursion.NAME);
        CSVReportGenerator reportGenerator = new CSVReportGenerator(tmpFile.toString(), finders);
        reportGenerator.generateReport(program, issues);
        reportGenerator.close();

        List<String> lines = Files.readAllLines(tmpFile);
        tmpFile.toFile().delete();

        assertThat(lines).hasSize(2);
        assertThat(lines.get(0)).isEqualTo("project,endless_recursion");
        assertThat(lines.get(1)).isEqualTo("recursiveProcedure,1");
    }

    @Test
    public void testSingleIssueTwoProjectsAppendCSV() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/bugpattern/recursiveProcedure.json");
        EndlessRecursion finder = new EndlessRecursion();
        Set<Issue> issues = finder.check(program);

        Path tmpFile = Files.createTempFile("foo", "bar");
        List<String> finders = new ArrayList<>();
        finders.add(EndlessRecursion.NAME);
        CSVReportGenerator reportGenerator = new CSVReportGenerator(tmpFile.toString(), finders);
        reportGenerator.generateReport(program, issues);
        reportGenerator.close();

        // Now write same issue again, which should only append
        finders = new ArrayList<>();
        finders.add(EndlessRecursion.NAME);
        reportGenerator = new CSVReportGenerator(tmpFile.toString(), finders);
        reportGenerator.generateReport(program, issues);
        reportGenerator.close();

        List<String> lines = Files.readAllLines(tmpFile);
        tmpFile.toFile().delete();

        assertThat(lines).hasSize(3);
        assertThat(lines.get(0)).isEqualTo("project,endless_recursion");
        assertThat(lines.get(1)).isEqualTo("recursiveProcedure,1");
        assertThat(lines.get(2)).isEqualTo("recursiveProcedure,1");
    }
}
