package newanalytics.smells;

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

public class UnusedVariableTest {
    private static Program empty;
    private static Program unusedVariables;
    private static Program oneUsedVariables;
    private static ObjectMapper mapper = new ObjectMapper();

    @BeforeAll
    public static void setUp() throws IOException, ParsingException {

        File f = new File("./src/test/fixtures/bugpattern/recursion.json");
        empty = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/smells/unusedVariables.json");
        unusedVariables = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/smells/oneUsedVariable.json");
        oneUsedVariables = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
    }

    @Test
    public void testEmptyProgram() {
        UnusedVariable parameterName = new UnusedVariable();
        IssueReport report = parameterName.check(empty);
        Assertions.assertEquals(0, report.getCount());
    }

    @Test
    public void testUnusedVariable() {
        UnusedVariable parameterName = new UnusedVariable();
        IssueReport report = parameterName.check(unusedVariables);
        Assertions.assertEquals(2, report.getCount());
    }

    @Test
    public void testOneUsedVariable() {
        UnusedVariable parameterName = new UnusedVariable();
        IssueReport report = parameterName.check(oneUsedVariables);
        Assertions.assertEquals(2, report.getCount());
    }
}
