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

public class AmbiguousProcedureSignatureTest {
    private static Program empty;
    private static Program ambiguousProcedure;
    private static Program ambiguousProcedureDiffArg;
    private static ObjectMapper mapper = new ObjectMapper();

    @BeforeAll
    public static void setUp() throws IOException, ParsingException {

        File f = new File("./src/test/fixtures/emptyProject.json");
        empty = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/bugpattern/ambiguousProcedureSignature.json");
        ambiguousProcedure = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/bugpattern/ambiguousSignatureDiffArg.json");
        ambiguousProcedureDiffArg = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
    }

    @Test
    public void testEmptyProgram() {
        AmbiguousProcedureSignature parameterName = new AmbiguousProcedureSignature();
        IssueReport report = parameterName.check(empty);
        Assertions.assertEquals(0, report.getCount());
    }

    @Test
    public void testAmbiguousSignatures() {
        AmbiguousProcedureSignature parameterName = new AmbiguousProcedureSignature();
        IssueReport report = parameterName.check(ambiguousProcedure);
        Assertions.assertEquals(2, report.getCount());
    }

    @Test
    public void testAmbiguousSigDifferentParameters() {
        AmbiguousProcedureSignature parameterName = new AmbiguousProcedureSignature();
        IssueReport report = parameterName.check(ambiguousProcedureDiffArg);
        Assertions.assertEquals(2, report.getCount());
    }

}
