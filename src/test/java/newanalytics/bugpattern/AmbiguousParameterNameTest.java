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

public class AmbiguousParameterNameTest {
    private static Program empty;
    private static Program ambiguousParams;
    private static Program clans;
    private static ObjectMapper mapper = new ObjectMapper();

    @BeforeAll
    public static void setUp() throws IOException, ParsingException {

        File f = new File("./src/test/fixtures/emptyProject.json");
        empty = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/bugpattern/ambiguousParameters.json");
        ambiguousParams= ProgramParser.parseProgram(f.getName(),mapper.readTree(f));
        f = new File("./src/test/fixtures/bugpattern/clans.json");
        clans= ProgramParser.parseProgram(f.getName(),mapper.readTree(f));
    }

    @Test
    public void testEmptyProgram() {
        AmbiguousParameterName parameterName = new AmbiguousParameterName();
        IssueReport report = parameterName.check(empty);
        Assertions.assertEquals(0,report.getCount() );
    }

    @Test
    public void testAmbiguousParameters(){
        AmbiguousParameterName parameterName = new AmbiguousParameterName();
        IssueReport report = parameterName.check(ambiguousParams);
        Assertions.assertEquals(2,report.getCount() );
    }

    @Test
    public void testClans(){
        AmbiguousParameterName parameterName = new AmbiguousParameterName();
        IssueReport report = parameterName.check(clans);
        Assertions.assertEquals(0,report.getCount() );
    }
}
