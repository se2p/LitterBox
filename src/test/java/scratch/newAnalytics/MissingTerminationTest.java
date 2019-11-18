package scratch.newAnalytics;

import newanalytics.IssueReport;
import com.fasterxml.jackson.databind.ObjectMapper;
import newanalytics.smells.MissingTermination;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import scratch.newast.ParsingException;
import scratch.newast.model.Program;
import scratch.newast.parser.ProgramParser;

import java.io.File;
import java.io.IOException;

public class MissingTerminationTest {
    ObjectMapper mapper = new ObjectMapper();
    Program program;

    @BeforeAll
    public void setUp() throws IOException, ParsingException {
        File f = new File("./src/test/java/scratch/fixtures/missingTermination.json");
        program = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        IssueReport report = (new MissingTermination()).check(program);
    }

    @Test
    public void testMissingTermination(){

    }

}
