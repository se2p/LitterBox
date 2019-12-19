package scratch.ast.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import newanalytics.IssueReport;
import newanalytics.bugpattern.ExpressionAsColor;
import newanalytics.utils.SpriteCount;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import scratch.ast.ParsingException;
import scratch.ast.model.Program;

import java.io.File;
import java.io.IOException;

public class ListAsBooleanTest {
    private static Program empty;
    private static ObjectMapper mapper = new ObjectMapper();

    @BeforeAll
    public static void setUp() throws IOException, ParsingException {

        File f = new File("./src/test/fixtures/stmtParser/listElementsBoolean.json");
        empty = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));

    }

    @Test
    public void testEmptyProgram() {
        SpriteCount sp = new SpriteCount();
        IssueReport rep = sp.check(empty);
        Assertions.assertEquals(1,rep.getCount());
    }
}
