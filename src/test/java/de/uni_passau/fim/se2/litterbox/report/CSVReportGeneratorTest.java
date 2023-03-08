/*
 * Copyright (C) 2019-2022 LitterBox contributors
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
import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueTool;
import de.uni_passau.fim.se2.litterbox.analytics.bugpattern.EndlessRecursion;
import de.uni_passau.fim.se2.litterbox.analytics.metric.ProcedureCount;
import de.uni_passau.fim.se2.litterbox.analytics.metric.ScriptCount;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

import static com.google.common.truth.Truth.assertThat;
import static de.uni_passau.fim.se2.litterbox.utils.GroupConstants.BUGS;

public class CSVReportGeneratorTest implements JsonTest {

    @Test
    public void testSingleIssueSingleProjectNewCSV() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/bugpattern/recursiveProcedure.json");
        EndlessRecursion finder = new EndlessRecursion();
        Set<Issue> issues = finder.check(program);

        Path tmpFile = Files.createTempFile("foo", "bar");
        List<String> finders = new ArrayList<>();
        finders.add(EndlessRecursion.NAME);
        CSVReportGenerator reportGenerator = new CSVReportGenerator(tmpFile.toString(), finders, false);
        reportGenerator.generateReport(program, issues);
        reportGenerator.close();

        List<String> lines = Files.readAllLines(tmpFile);
        Files.delete(tmpFile);

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
        CSVReportGenerator reportGenerator = new CSVReportGenerator(tmpFile.toString(), finders, false);
        reportGenerator.generateReport(program, issues);
        reportGenerator.close();

        // Now write same issue again, which should only append
        finders = new ArrayList<>();
        finders.add(EndlessRecursion.NAME);
        reportGenerator = new CSVReportGenerator(tmpFile.toString(), finders, false);
        reportGenerator.generateReport(program, issues);
        reportGenerator.close();

        List<String> lines = Files.readAllLines(tmpFile);
        Files.delete(tmpFile);

        assertThat(lines).hasSize(3);
        assertThat(lines.get(0)).isEqualTo("project,endless_recursion");
        assertThat(lines.get(1)).isEqualTo("recursiveProcedure,1");
        assertThat(lines.get(2)).isEqualTo("recursiveProcedure,1");
    }

    @Test
    public void testGenerateReportsPerScript() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/bugsPerScripts/random_project.json");
        List<IssueFinder> issueFinders = IssueTool.getFinders(BUGS);

        Set<Issue> issues = new LinkedHashSet<>();
        for (IssueFinder iF : issueFinders) {
            iF.setIgnoreLooseBlocks(true);
            issues.addAll(iF.check(program));
        }

        Path tmpFile = Files.createTempFile("foo", "bar");
        List<String> finders = new ArrayList<>(IssueTool.getBugFinderNames());
        CSVReportGenerator reportGenerator = new CSVReportGenerator(tmpFile.toString(), finders, true);
        reportGenerator.generateReport(program, issues);
        reportGenerator.close();

        List<String> lines = Files.readAllLines(tmpFile);
        Files.delete(tmpFile);

        ScriptCount<ASTNode> scriptCount = new ScriptCount<>();
        int scriptCountPerProgram = (int) scriptCount.calculateMetric(program);

        ProcedureCount<ASTNode> procedureCount = new ProcedureCount<>();
        int procedureCountPerProgram = (int) procedureCount.calculateMetric(program);

        assertThat(lines).hasSize(1 + scriptCountPerProgram + procedureCountPerProgram);
        assertThat(lines.get(0)).isEqualTo("script," + String.join(",", finders));

        int totalBugsInScripts = getTotalBugsCountInScripts(lines);
        assertThat(totalBugsInScripts).isEqualTo(issues.size());
    }

    private int getTotalBugsCountInScripts(List<String> lines) {
        int count = 0;
        for (String line : lines.subList(1, lines.size())) {
            var lineSplits = line.split(",");
            String[] bugsCount = Arrays.copyOfRange(lineSplits, 1, lineSplits.length);
            int[] arr = Stream.of(bugsCount).mapToInt(Integer::parseInt).toArray();
            count += Arrays.stream(arr).sum();
        }
        return count;
    }
}
