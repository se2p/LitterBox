package scratch.newAnalytics;

import newanalytics.IssueReport;
import com.fasterxml.jackson.databind.ObjectMapper;
import newanalytics.smells.MissingTermination;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import scratch.newast.ParsingException;
import scratch.newast.model.Program;
import scratch.newast.parser.ProgramParser;
import org.junit.jupiter.api.Assertions;

import java.io.File;
import java.io.IOException;

public class MissingTerminationTest {
    private static ObjectMapper mapper = new ObjectMapper();
    private static Program program;
    private static Program programNested;

    @BeforeAll
    public static void setUp() throws IOException, ParsingException {
        File f = new File("./src/test/java/scratch/fixtures/missingTermination.json");
        program = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f= new File("./src/test/java/scratch/fixtures/missingTerminationNested.json");
        programNested = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
    }

    @Test
    public void testMissingTermination() {
        IssueReport report = (new MissingTermination()).check(program);
        Assertions.assertEquals(1, report.getCount());
    }

    @Test
    public void testMissingTerminationNested(){
        IssueReport report = (new MissingTermination()).check(programNested);
        Assertions.assertEquals(1, report.getCount());
    }
}
