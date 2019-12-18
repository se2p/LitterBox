package newanalytics.bugpattern;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.truth.Truth;
import newanalytics.IssueReport;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import scratch.ast.ParsingException;
import scratch.ast.model.Program;
import scratch.ast.parser.ProgramParser;

import java.io.File;
import java.io.IOException;

import static junit.framework.TestCase.fail;

class ComparingLiteralsTest {

    private static Program program;
    private static Program emptyFields;

    @BeforeAll
    public static void setup() {
        String path = "src/test/fixtures/bugpattern/comparingLiterals.json";
        File file = new File(path);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            program = ProgramParser.parseProgram("comparing literals", objectMapper.readTree(file));
            file = new File("./src/test/fixtures/bugpattern/twoNotColo.json");
            emptyFields =ProgramParser.parseProgram("comparing empty literals", objectMapper.readTree(file));
        } catch (IOException | ParsingException e) {
            fail();
        }
    }

    @Test
    public void testMissingPenUp() {
        ComparingLiterals finder = new ComparingLiterals();
        final IssueReport check = finder.check(program);
        Truth.assertThat(check.getCount()).isEqualTo(12);
        Truth.assertThat(check.getPosition().get(0)).isEqualTo("Sprite1");
    }

    @Test
    public void testEmptyCompare() {
        ComparingLiterals finder = new ComparingLiterals();
        final IssueReport check = finder.check(emptyFields);
        Truth.assertThat(check.getCount()).isEqualTo(1);
    }
}