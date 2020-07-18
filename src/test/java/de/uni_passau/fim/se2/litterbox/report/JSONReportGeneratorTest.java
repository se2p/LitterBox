package de.uni_passau.fim.se2.litterbox.report;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.bugpattern.PositionEqualsCheck;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParser;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONReportGeneratorTest {

    // TODO: This is a clone now
    private Program getAST(String fileName) throws IOException, ParsingException {
        File file = new File(fileName);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode project = objectMapper.readTree(file);
        Program program = ProgramParser.parseProgram("TestProgram", project);
        return program;
    }

    @Test
    public void testSingleIssue() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/bugpattern/xPosEqual.json");
        PositionEqualsCheck finder = new PositionEqualsCheck();
        Set<Issue> issues = finder.check(program);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        JSONReportGenerator generator = new JSONReportGenerator(os);
        generator.generateReport(program, issues);
        os.close();
        assertEquals("[ {\n" +
                "  \"finder\" : \"position_equals_check\",\n" +
                "  \"sprite\" : \"Sprite1\",\n" +
                "  \"hint\" : \"position equals check\",\n" +
                "  \"code\" : \"[scratchblocks]\\n<(x position) = :: #ff0000> // Issue: position equals check\\n[/scratchblocks]\\n\"\n" +
                "} ]", os.toString());
    }

    @Test
    public void testMultipleIssues() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/bugpattern/positionEqualsCheck.json");
        PositionEqualsCheck finder = new PositionEqualsCheck();
        Set<Issue> issues = finder.check(program);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        JSONReportGenerator generator = new JSONReportGenerator(os);
        generator.generateReport(program, issues);
        os.close();
        assertEquals("[ {\n" +
                "  \"finder\" : \"position_equals_check\",\n" +
                "  \"sprite\" : \"Figur1\",\n" +
                "  \"hint\" : \"position equals check\",\n" +
                "  \"code\" : \"[scratchblocks]\\n<(distance to (mouse-pointer v)) = :: #ff0000> // Issue: position equals check\\n[/scratchblocks]\\n\"\n" +
                "}, {\n" +
                "  \"finder\" : \"position_equals_check\",\n" +
                "  \"sprite\" : \"Figur1\",\n" +
                "  \"hint\" : \"position equals check\",\n" +
                "  \"code\" : \"[scratchblocks]\\n<(distance to (Bat v)) = :: #ff0000> // Issue: position equals check\\n[/scratchblocks]\\n\"\n" +
                "}, {\n" +
                "  \"finder\" : \"position_equals_check\",\n" +
                "  \"sprite\" : \"Figur1\",\n" +
                "  \"hint\" : \"position equals check\",\n" +
                "  \"code\" : \"[scratchblocks]\\nwait until <(mouse x) = >:: #ff0000 // Issue: position equals check\\n[/scratchblocks]\\n\"\n" +
                "}, {\n" +
                "  \"finder\" : \"position_equals_check\",\n" +
                "  \"sprite\" : \"Figur1\",\n" +
                "  \"hint\" : \"position equals check\",\n" +
                "  \"code\" : \"[scratchblocks]\\nwait until <(mouse y) = >:: #ff0000 // Issue: position equals check\\n[/scratchblocks]\\n\"\n" +
                "} ]", os.toString());
    }

    @Test
    public void testFileOutput() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/bugpattern/xPosEqual.json");
        PositionEqualsCheck finder = new PositionEqualsCheck();
        Set<Issue> issues = finder.check(program);

        Path tmpFile = Files.createTempFile(null, null);
        JSONReportGenerator generator = new JSONReportGenerator(tmpFile.toString());
        generator.generateReport(program, issues);

        String result = Files.readString(tmpFile);
        assertEquals("[ {\n" +
                "  \"finder\" : \"position_equals_check\",\n" +
                "  \"sprite\" : \"Sprite1\",\n" +
                "  \"hint\" : \"position equals check\",\n" +
                "  \"code\" : \"[scratchblocks]\\n<(x position) = :: #ff0000> // Issue: position equals check\\n[/scratchblocks]\\n\"\n" +
                "} ]", result);
        Files.delete(tmpFile);
    }
}
