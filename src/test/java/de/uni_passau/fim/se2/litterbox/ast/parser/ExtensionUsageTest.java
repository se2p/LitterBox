package de.uni_passau.fim.se2.litterbox.ast.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static junit.framework.TestCase.fail;

public class ExtensionUsageTest {

        private static JsonNode project;

        @BeforeAll
        public static void setup() {
            String path = "src/test/fixtures/testExtension.json";
            File file = new File(path);
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                project = objectMapper.readTree(file);
            } catch (IOException e) {
                fail();
            }
        }
    @Test
    public void testContains() throws ParsingException {
        Program program = ProgramParser.parseProgram("Extension", project);
    }
}
