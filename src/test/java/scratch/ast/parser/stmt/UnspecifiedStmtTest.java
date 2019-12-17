package scratch.ast.parser.stmt;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import scratch.ast.ParsingException;
import scratch.ast.model.Program;
import scratch.ast.model.statement.UnspecifiedStmt;
import scratch.ast.parser.ProgramParser;

import java.io.File;
import java.io.IOException;

public class UnspecifiedStmtTest {

    private static Program unusedProc;
    private static ObjectMapper mapper = new ObjectMapper();

    @BeforeAll
    public static void setUp() throws IOException, ParsingException {

        File f = new File("./src/test/fixtures/stmtParser/textToSpeech.json");
        unusedProc = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
    }

    @Test
    public void parseTextToSpeechTest() {
        Assertions.assertTrue(unusedProc.getActorDefinitionList().getDefintions().get(1).getScripts().getScriptList().get(0).getStmtList().getStmts().getListOfStmt().get(0) instanceof UnspecifiedStmt);
    }
}
