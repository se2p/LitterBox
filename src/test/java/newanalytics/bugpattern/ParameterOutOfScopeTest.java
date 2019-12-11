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

public class ParameterOutOfScopeTest {
    private static Program empty;
    private static Program orphanedParam;
    private static Program outsideParam;
    private static ObjectMapper mapper = new ObjectMapper();

    @BeforeAll
    public static void setUp() throws IOException, ParsingException {

        File f = new File("./src/test/fixtures/emptyProject.json");
        empty = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/bugpattern/orphanedParameter.json");
        orphanedParam = ProgramParser.parseProgram(f.getName(),mapper.readTree(f));
        f = new File("./src/test/fixtures/bugpattern/parameterOutsideScope.json");
        outsideParam = ProgramParser.parseProgram(f.getName(),mapper.readTree(f));
    }

    @Test
    public void testEmptyProgram() {
       ParameterOutOfScope parameterName = new ParameterOutOfScope();
        IssueReport report = parameterName.check(empty);
        Assertions.assertEquals(0,report.getCount() );
    }

    @Test
    public void testOrphanedParameter(){
        ParameterOutOfScope parameterName = new ParameterOutOfScope();
        IssueReport report = parameterName.check(orphanedParam);
        Assertions.assertEquals(0,report.getCount() );
    }

    @Test
    public void testOutsideParameter(){
        ParameterOutOfScope parameterName = new ParameterOutOfScope();
        IssueReport report = parameterName.check(outsideParam);
        Assertions.assertEquals(1,report.getCount() );
    }
}
