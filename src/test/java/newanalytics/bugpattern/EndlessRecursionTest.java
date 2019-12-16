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

public class EndlessRecursionTest {
    private static Program empty;
    private static Program endlessRecursion;
    private static Program recursion;
    private static ObjectMapper mapper = new ObjectMapper();

    @BeforeAll
    public static void setUp() throws IOException, ParsingException {

        File f = new File("./src/test/fixtures/emptyProject.json");
        empty = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/bugpattern/recursiveProcedure.json");
        endlessRecursion = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/bugpattern/recursion.json");
        recursion = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
    }

    @Test
    public void testEmptyProgram() {
        EndlessRecursion parameterName = new EndlessRecursion();
        IssueReport report = parameterName.check(empty);
        Assertions.assertEquals(0, report.getCount());
    }

    @Test
    public void testEndlessRecursion() {
        EndlessRecursion parameterName = new EndlessRecursion();
        IssueReport report = parameterName.check(endlessRecursion);
        Assertions.assertEquals(1, report.getCount());
    }

    @Test
    public void testRecursion() {
        EndlessRecursion parameterName = new EndlessRecursion();
        IssueReport report = parameterName.check(recursion);
        Assertions.assertEquals(0, report.getCount());
    }
}
