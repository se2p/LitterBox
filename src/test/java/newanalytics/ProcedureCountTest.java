package newanalytics;

import com.fasterxml.jackson.databind.ObjectMapper;
import newanalytics.utils.ProcedureCount;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import scratch.ast.ParsingException;
import scratch.ast.model.Program;
import scratch.ast.parser.ProgramParser;

import java.io.File;
import java.io.IOException;

public class ProcedureCountTest {
    private static Program empty;
    private static Program unusedProc;
    private static ObjectMapper mapper = new ObjectMapper();

    @BeforeAll
    public static void setUp() throws IOException, ParsingException {

        File f = new File("./src/test/fixtures/emptyProject.json");
        empty = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/smells/unusedEmptyProcedure.json");
        unusedProc = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
    }

    @Test
    public void testEmptyProgram() {
        ProcedureCount parameterName = new  ProcedureCount();
        IssueReport report = parameterName.check(empty);
        Assertions.assertEquals(0, report.getCount());
    }

    @Test
    public void testProcCount() {
        ProcedureCount parameterName = new  ProcedureCount();
        IssueReport report = parameterName.check(unusedProc);
        Assertions.assertEquals(2, report.getCount());
    }
}
