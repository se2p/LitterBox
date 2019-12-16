package newanalytics.bugpattern;

import com.fasterxml.jackson.databind.ObjectMapper;
import newanalytics.IssueReport;
import newanalytics.smells.EmptyBody;
import newanalytics.smells.StutteringMovement;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import scratch.ast.ParsingException;
import scratch.ast.model.Program;
import scratch.ast.parser.ProgramParser;

import java.io.File;
import java.io.IOException;

public class StutteringMovementTest {
    private static Program stutteringMovement;
    private static ObjectMapper mapper = new ObjectMapper();

    @BeforeAll
    public static void setUp() throws IOException, ParsingException {

        File f = new File("./src/test/fixtures/bugpattern/stutteringMovement.json");
        stutteringMovement = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
    }

    @Test
    public void testStutteringMovement() {
        StutteringMovement finder = new StutteringMovement();
        IssueReport report = finder.check(stutteringMovement);
        Assertions.assertEquals(7, report.getCount());
    }
}
