package newanalytics;

import com.fasterxml.jackson.databind.ObjectMapper;
import newanalytics.utils.SpriteCount;
import newanalytics.utils.WeightedMethodCount;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import scratch.ast.ParsingException;
import scratch.ast.model.Program;
import scratch.ast.parser.ProgramParser;

import java.io.File;
import java.io.IOException;

public class WeightedMethodCountTest {

    private static Program empty;
    private static Program unusedProc;
    private static ObjectMapper mapper = new ObjectMapper();

    @BeforeAll
    public static void setUp() throws IOException, ParsingException {

        File f = new File("./src/test/fixtures/emptyProject.json");
        empty = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/weightedMethod.json");
        unusedProc = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
    }

    @Test
    public void testEmptyProgram() {
        WeightedMethodCount parameterName = new WeightedMethodCount();
        IssueReport report = parameterName.check(empty);
        Assertions.assertEquals(1, report.getCount());
    }

    @Test
    public void testMethodCount() {
        WeightedMethodCount parameterName = new  WeightedMethodCount();
        IssueReport report = parameterName.check(unusedProc);
        Assertions.assertEquals(5, report.getCount());
    }
}
