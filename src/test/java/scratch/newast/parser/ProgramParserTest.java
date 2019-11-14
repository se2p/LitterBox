package scratch.newast.parser;

import static junit.framework.TestCase.fail;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.truth.Truth;
import java.io.File;
import java.io.IOException;
import org.junit.Test;
import scratch.newast.ParsingException;
import scratch.newast.model.ActorDefinitionList;
import scratch.newast.model.Program;

public class ProgramParserTest {

    @Test
    public void parseEmptyProgram() {
        String path = "src/test/java/scratch/fixtures/emptyProject.json";
        File file = new File(path);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode emptyProject = objectMapper.readTree(file);
            Program program = ProgramParser.parseProgram("Empty", emptyProject);
            Truth.assertThat(program.getIdent().getValue()).isEqualTo("Empty");
            Truth.assertThat(program.getChildren().size()).isEqualTo(2);
            Truth.assertThat(program.getChildren().get(1)).isInstanceOf(ActorDefinitionList.class);

            ActorDefinitionList list = program.getActorDefinitionList();
            Truth.assertThat(list.getActorDefinitionList().size()).isEqualTo(2);
        } catch (ParsingException | IOException e) {
            e.printStackTrace();
            fail();
        }
    }
}