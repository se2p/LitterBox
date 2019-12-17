package newanalytics;

import com.fasterxml.jackson.databind.ObjectMapper;
import newanalytics.utils.BlockCount;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import scratch.ast.ParsingException;
import scratch.ast.model.Program;
import scratch.ast.parser.ProgramParser;


import java.io.File;
import java.io.IOException;

public class BlockCountTest {
    private static Program empty;
    private static Program nestedLoops;
    private static Program withproc;
    private static ObjectMapper mapper = new ObjectMapper();

    @BeforeAll
    public static void setUp() throws IOException, ParsingException {

        File f = new File("./src/test/fixtures/emptyProject.json");
        empty = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/smells/nestedLoops.json");
        nestedLoops = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/blockCountWithProc.json");
        withproc = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
    }

    @Test
    public void testEmptyProgram() {
        BlockCount parameterName = new BlockCount();
        IssueReport report = parameterName.check(empty);
        Assertions.assertEquals(0, report.getCount());
    }

    @Test
    public void testBlockCountNested() {
        BlockCount parameterName = new BlockCount();
        IssueReport report = parameterName.check(nestedLoops);
        Assertions.assertEquals(16, report.getCount());
    }

    @Test
    public void testBlockproc() {
        BlockCount parameterName = new BlockCount();
        IssueReport report = parameterName.check(withproc);
        Assertions.assertEquals(23, report.getCount());
    }
}
