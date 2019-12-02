package scratch.ast.parser;

import static junit.framework.TestCase.fail;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.truth.Truth;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import scratch.ast.Constants;
import scratch.ast.ParsingException;
import scratch.ast.model.ActorDefinition;
import scratch.ast.model.Program;
import scratch.ast.model.procedure.ProcedureDefinition;
import scratch.ast.model.type.BooleanType;
import scratch.ast.model.type.StringType;

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
            Assertions.assertEquals("BlockNoInputs",
                    ProgramParser.procDefMap.getProcedures().get(list.get(0).getIdent()).getName());
            Assertions.assertEquals(0,
                    ProgramParser.procDefMap.getProcedures().get(list.get(0).getIdent()).getArguments().length);
            Assertions.assertEquals(0, list.get(0).getParameterList().getParameterListPlain().getParameters().size());
            Assertions.assertEquals(3, list.get(0).getStmtList().getStmts().getListOfStmt().size());

        } catch (ParsingException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testInputTest() {
        try {
            Program program = ProgramParser.parseProgram("CustomBlockTest", project);
            final List<ActorDefinition> defintions = program.getActorDefinitionList().getDefintions();
            final List<ProcedureDefinition> list = defintions.get(1).getProcedureDefinitionList().getList();

            Truth.assertThat(list.get(1)).isInstanceOf(ProcedureDefinition.class);
            Assertions.assertEquals("BlockWithInputs %s %b",
                    ProgramParser.procDefMap.getProcedures().get(list.get(1).getIdent()).getName());
            Assertions.assertEquals(2,
                    ProgramParser.procDefMap.getProcedures().get(list.get(1).getIdent()).getArguments().length);
            Assertions.assertEquals(Constants.PARAMETER_ABBREVIATION + "NumInput",
                    ProgramParser.procDefMap.getProcedures().get(list.get(1).getIdent()).getArguments()[0].getName());
            Assertions.assertEquals(Constants.PARAMETER_ABBREVIATION + "Boolean",
                    ProgramParser.procDefMap.getProcedures().get(list.get(1).getIdent()).getArguments()[1].getName());
            Truth.assertThat(ProgramParser.procDefMap.getProcedures().get(list.get(1).getIdent()).getArguments()[0].getType()).isInstanceOf(StringType.class);
            Truth.assertThat(ProgramParser.procDefMap.getProcedures().get(list.get(1).getIdent()).getArguments()[1].getType()).isInstanceOf(BooleanType.class);
            Assertions.assertEquals(3, list.get(1).getStmtList().getStmts().getListOfStmt().size());
            Assertions.assertEquals(ProgramParser.procDefMap.getProcedures().get(list.get(1).getIdent()).getArguments()[1].getName(), list.get(1).getParameterList().getParameterListPlain().getParameters().get(1).getIdent().getName());
            Assertions.assertEquals(ProgramParser.procDefMap.getProcedures().get(list.get(1).getIdent()).getArguments()[0].getName(), list.get(1).getParameterList().getParameterListPlain().getParameters().get(0).getIdent().getName());
            Assertions.assertEquals(ProgramParser.procDefMap.getProcedures().get(list.get(1).getIdent()).getArguments()[1].getType(), list.get(1).getParameterList().getParameterListPlain().getParameters().get(1).getType());
            Assertions.assertEquals(ProgramParser.procDefMap.getProcedures().get(list.get(1).getIdent()).getArguments()[0].getType(), list.get(1).getParameterList().getParameterListPlain().getParameters().get(0).getType());
        } catch (ParsingException e) {
            e.printStackTrace();
            fail();
        }
    }
}
