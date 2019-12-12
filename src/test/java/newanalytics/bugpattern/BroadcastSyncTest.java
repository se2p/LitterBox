package newanalytics.bugpattern;

import static junit.framework.TestCase.fail;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.truth.Truth;
import java.io.File;
import java.io.IOException;
import newanalytics.IssueReport;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import scratch.ast.ParsingException;
import scratch.ast.model.Program;
import scratch.ast.parser.ProgramParser;

class BroadcastSyncTest {
    private static Program program;

    @BeforeAll
    public static void setup() {
        String path = "src/test/fixtures/bugpattern/broadcastSync.json";
        File file = new File(path);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            program = ProgramParser.parseProgram("broadcastSync", objectMapper.readTree(file));
        } catch (IOException | ParsingException e) {
            fail();
        }
    }

    @Test
    public void testMissingPenUp() {
        BroadcastSync finder = new BroadcastSync();
        final IssueReport check = finder.check(program);
        Truth.assertThat(check.getCount()).isEqualTo(5);
        Truth.assertThat(check.getPosition().get(0)).isEqualTo("Apple");
        Truth.assertThat(check.getPosition().get(1)).isEqualTo("Sprite1");
        Truth.assertThat(check.getPosition().get(2)).isEqualTo("Abby");
    }
}