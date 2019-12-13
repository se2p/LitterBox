package newanalytics.bugpattern;

import com.fasterxml.jackson.databind.ObjectMapper;
import newanalytics.IssueReport;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import scratch.ast.ParsingException;
import scratch.ast.model.Program;
import scratch.ast.parser.ProgramParser;

import java.io.File;
import java.io.IOException;

public class EqualsConditionTest {
    private static Program empty;
    private static Program equalCond;
    private static ObjectMapper mapper = new ObjectMapper();

    @BeforeAll
    public static void setUp() throws IOException, ParsingException {

        File f = new File("./src/test/fixtures/emptyProject.json");
        empty = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/bugpattern/equalsCondition.json");
        equalCond = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));

    }

    @Test
    public void testEmptyProgram() {
        EqualsCondition parameterName = new EqualsCondition();
        IssueReport report = parameterName.check(empty);
        Assertions.assertEquals(0, report.getCount());
    }

    @Test
    public void testEqualCond() {
        EqualsCondition parameterName = new EqualsCondition();
        IssueReport report = parameterName.check(equalCond);
        Assertions.assertEquals(7, report.getCount());
    }
}
