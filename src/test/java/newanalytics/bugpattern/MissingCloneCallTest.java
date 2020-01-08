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

public class MissingCloneCallTest {
    private static Program empty;
    private static Program missingCloneCall;
    private static Program cloneInOtherSprite;
    private static Program rainbowSix;
    private static Program jumper;
    private static Program superMario;
    private static ObjectMapper mapper = new ObjectMapper();

    @BeforeAll
    public static void setUp() throws IOException, ParsingException {

        File f = new File("./src/test/fixtures/emptyProject.json");
        empty = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/bugpattern/missingCloneCall.json");
        missingCloneCall = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/bugpattern/cloneInOtherSprite.json");
        cloneInOtherSprite = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/bugpattern/rainbowSix.json");
        rainbowSix = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/bugpattern/jumper.json");
        jumper = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/bugpattern/superMario.json");
        superMario = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
    }

    @Test
    public void testEmptyProgram() {
        MissingCloneCall parameterName = new MissingCloneCall();
        IssueReport report = parameterName.check(empty);
        Assertions.assertEquals(0, report.getCount());
    }

    @Test
    public void testMissingCloneCall() {
        MissingCloneCall parameterName = new MissingCloneCall();
        IssueReport report = parameterName.check(missingCloneCall);
        Assertions.assertEquals(1, report.getCount());
    }

    @Test
    public void testCloneInOtherSprite() {
        MissingCloneCall parameterName = new MissingCloneCall();
        IssueReport report = parameterName.check(cloneInOtherSprite);
        Assertions.assertEquals(0, report.getCount());
    }

    @Test
    public void testRainbowSix() {
        MissingCloneCall parameterName = new MissingCloneCall();
        IssueReport report = parameterName.check(rainbowSix);
        Assertions.assertEquals(0, report.getCount());
    }

    @Test
    public void testJumper() {
        MissingCloneCall parameterName = new MissingCloneCall();
        IssueReport report = parameterName.check(jumper);
        Assertions.assertEquals(1, report.getCount());
    }

    @Test
    public void testSuperMario() {
        MissingCloneCall parameterName = new MissingCloneCall();
        IssueReport report = parameterName.check(superMario);
        Assertions.assertEquals(1, report.getCount());
    }
}
