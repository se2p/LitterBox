package newanalytics.bugpattern;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import scratch.ast.ParsingException;
import scratch.ast.model.Program;
import scratch.ast.parser.ProgramParser;

import java.io.File;
import java.io.IOException;

import static junit.framework.TestCase.fail;

class MissingPenUpTest {

    private static Program program;

    @BeforeAll
    public static void setup() {
        String path = "src/test/fixtures/bugpattern/missingPenUp.json";
        File file = new File(path);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            program = ProgramParser.parseProgram("missingPenUp", objectMapper.readTree(file));
        } catch (IOException | ParsingException e) {
            fail();
        }
    }

    @Test
    public void testMissingPenUp() {
        MissingPenUp finder = new MissingPenUp();
        finder.check(program);
    }
}