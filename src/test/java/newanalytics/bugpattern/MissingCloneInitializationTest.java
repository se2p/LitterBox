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

public class MissingCloneInitializationTest {

    private static Program program;

    @BeforeAll
    public static void setup() {
        String path = "src/test/fixtures/bugpattern/missingCloneInitialization.json";
        File file = new File(path);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            program = ProgramParser.parseProgram("missingCloneInit", objectMapper.readTree(file));
        } catch (IOException | ParsingException e) {
            fail();
        }
    }

    @Test
    public void testMissingPenUp() {
        MissingCloneInitialization finder = new MissingCloneInitialization();
        final IssueReport check = finder.check(program);
        Truth.assertThat(check.getCount()).isEqualTo(1);
        Truth.assertThat(check.getPosition().get(0)).isEqualTo("Anina Dance");
    }


}