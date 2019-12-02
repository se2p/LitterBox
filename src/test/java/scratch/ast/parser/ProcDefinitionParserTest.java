package scratch.ast.parser;

import static junit.framework.TestCase.fail;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.truth.Truth;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import scratch.ast.ParsingException;
import scratch.ast.model.ActorDefinition;
import scratch.ast.model.Program;
import scratch.ast.model.procedure.ProcedureDefinition;

public class ProcDefinitionParserTest {

    private static JsonNode project;

    @BeforeAll
    public static void setup() {
        String path = "src/test/fixtures/customBlocks.json";
        File file = new File(path);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            project = objectMapper.readTree(file);
        } catch (IOException e) {
            fail();
        }
    }

    @Test
    public void testNoInputTest() {
        try {
            Program program = ProgramParser.parseProgram("CustomBlockTest", project);
            final List<ActorDefinition> defintions = program.getActorDefinitionList().getDefintions();
            final List<ProcedureDefinition> list = defintions.get(1).getProcedureDefinitionList().getList();

            Truth.assertThat(list.get(0)).isInstanceOf(ProcedureDefinition.class);

        } catch (ParsingException e) {
            e.printStackTrace();
            fail();
        }
    }

}
