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

public class MissingLoopSensingTest {

    private static Program empty;
    private static Program codeHero;
    private static Program anina;
    private static ObjectMapper mapper = new ObjectMapper();

    @BeforeAll
    public static void setUp() throws IOException, ParsingException {

        File f = new File("./src/test/fixtures/emptyProject.json");
        empty = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/bugpattern/codeHero.json");
        codeHero = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/bugpattern/anina.json");
        anina = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
    }

    @Test
    public void testEmptyProgram() {
        MissingLoopSensing parameterName = new MissingLoopSensing();
        IssueReport report = parameterName.check(empty);
        Assertions.assertEquals(0, report.getCount());
    }

    @Test
    public void testMissingLoopSensing() {
        MissingLoopSensing parameterName = new MissingLoopSensing();
        IssueReport report = parameterName.check(codeHero);
        Assertions.assertEquals(2, report.getCount());
    }

    @Test
    public void testAnina() {
        MissingLoopSensing parameterName = new MissingLoopSensing();
        IssueReport report = parameterName.check(anina);
        Assertions.assertEquals(1, report.getCount());
    }
}
