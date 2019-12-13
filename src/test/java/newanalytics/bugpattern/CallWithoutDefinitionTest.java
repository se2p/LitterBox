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

public class CallWithoutDefinitionTest  {
    private static Program empty;
    private static Program callWithoutDef;
    private static ObjectMapper mapper = new ObjectMapper();

    @BeforeAll
    public static void setUp() throws IOException, ParsingException {

        File f = new File("./src/test/fixtures/emptyProject.json");
        empty = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/bugpattern/callWithoutDefinition.json");
        callWithoutDef = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
    }

    @Test
    public void testEmptyProgram() {
        CallWithoutDefinition parameterName = new CallWithoutDefinition();
        IssueReport report = parameterName.check(empty);
        Assertions.assertEquals(0, report.getCount());
    }

    @Test
    public void testCallWithoutDef() {
        CallWithoutDefinition parameterName = new CallWithoutDefinition();
        IssueReport report = parameterName.check(callWithoutDef);
        Assertions.assertEquals(1, report.getCount());
    }
}
