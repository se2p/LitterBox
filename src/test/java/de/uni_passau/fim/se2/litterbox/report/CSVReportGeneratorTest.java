package de.uni_passau.fim.se2.litterbox.report;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.bugpattern.EndlessRecursion;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParser;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.google.common.truth.Truth.assertThat;

public class CSVReportGeneratorTest {

    // TODO: Code clone
    private Program getAST(String fileName) throws IOException, ParsingException {
        File file = new File(fileName);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode project = objectMapper.readTree(file);
        Program program = ProgramParser.parseProgram("TestProgram", project);
        return program;
    }

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
        assertThat(lines.get(1)).isEqualTo("TestProgram,1");
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
        assertThat(lines.get(1)).isEqualTo("TestProgram,1");
        assertThat(lines.get(2)).isEqualTo("TestProgram,1");
    }
}
