package newanalytics.smells;

import com.fasterxml.jackson.databind.ObjectMapper;
import newanalytics.IssueReport;
import newanalytics.bugpattern.EqualsCondition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import scratch.ast.ParsingException;
import scratch.ast.model.Program;
import scratch.ast.parser.ProgramParser;

import java.io.File;
import java.io.IOException;

public class NestedLoopTest {
    private static Program empty;
    private static Program nestedLoops;
    private static ObjectMapper mapper = new ObjectMapper();

    @BeforeAll
    public static void setUp() throws IOException, ParsingException {

        File f = new File("./src/test/fixtures/emptyProject.json");
        empty = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/smells/nestedLoops.json");
        nestedLoops = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));

    }

    @Test
    public void testEmptyProgram() {
        NestedLoops parameterName = new  NestedLoops();
        IssueReport report = parameterName.check(empty);
        Assertions.assertEquals(0, report.getCount());
    }

    @Test
    public void testNestedLoops() {
        NestedLoops parameterName = new  NestedLoops();
        IssueReport report = parameterName.check(nestedLoops);
        Assertions.assertEquals(3, report.getCount());
    }
}
