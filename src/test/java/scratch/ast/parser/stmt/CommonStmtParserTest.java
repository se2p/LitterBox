package scratch.ast.parser.stmt;

import static junit.framework.TestCase.fail;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.truth.Truth;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import scratch.ast.ParsingException;
import scratch.ast.model.ActorDefinition;
import scratch.ast.model.ActorDefinitionList;
import scratch.ast.model.Program;
import scratch.ast.model.Script;
import scratch.ast.model.statement.Stmt;
import scratch.ast.model.statement.common.Broadcast;
import scratch.ast.model.statement.common.BroadcastAndWait;
import scratch.ast.model.statement.common.ChangeAttributeBy;
import scratch.ast.model.statement.common.ChangeVariableBy;
import scratch.ast.model.statement.common.CreateCloneOf;
import scratch.ast.model.statement.common.ResetTimer;
import scratch.ast.model.statement.common.SetAttributeTo;
import scratch.ast.model.statement.common.StopOtherScriptsInSprite;
import scratch.ast.model.statement.common.WaitSeconds;
import scratch.ast.model.statement.common.WaitUntil;
import scratch.ast.model.statement.termination.DeleteClone;
import scratch.ast.parser.ProgramParser;

public class CommonStmtParserTest {

    private JsonNode project;

    @Before
    public void setup() {
        String path = "src/test/java/scratch/fixtures/commonStmts.json";
        File file = new File(path);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            project = objectMapper.readTree(file);
        } catch (IOException e) {
            fail();
        }
    }

    @Test
    public void testProgramStructure() {
        try {
            Program program = ProgramParser.parseProgram("CommonStmts", project);
            ActorDefinitionList list = program.getActorDefinitionList();
            Truth.assertThat(list.getDefintions().size()).isEqualTo(2);
        } catch (ParsingException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testStmtsInSprite() {
        try {
            Program program = ProgramParser.parseProgram("CommonStmts", project);
            ActorDefinitionList list = program.getActorDefinitionList();
            ActorDefinition sprite = list.getDefintions().get(1);

            Script script = sprite.getScripts().getScriptList().get(0);
            List<Stmt> listOfStmt = script.getStmtList().getStmts().getListOfStmt();

            Truth.assertThat(listOfStmt.get(0).getClass()).isEqualTo(WaitSeconds.class);
            Truth.assertThat(listOfStmt.get(1).getClass()).isEqualTo(WaitUntil.class);
            Truth.assertThat(listOfStmt.get(2).getClass()).isEqualTo(StopOtherScriptsInSprite.class);
            Truth.assertThat(listOfStmt.get(3).getClass()).isEqualTo(CreateCloneOf.class);
            Truth.assertThat(listOfStmt.get(4).getClass()).isEqualTo(Broadcast.class);
            Truth.assertThat(listOfStmt.get(5).getClass()).isEqualTo(BroadcastAndWait.class);
            Truth.assertThat(listOfStmt.get(6).getClass()).isEqualTo(ResetTimer.class);
            Truth.assertThat(listOfStmt.get(7).getClass()).isEqualTo(ChangeVariableBy.class);
            Truth.assertThat(listOfStmt.get(8).getClass()).isEqualTo(ChangeAttributeBy.class);
            Truth.assertThat(listOfStmt.get(9).getClass()).isEqualTo(SetAttributeTo.class);
            Truth.assertThat(listOfStmt.get(10).getClass()).isEqualTo(ChangeAttributeBy.class);
            Truth.assertThat(script.getStmtList().getTerminationStmt().getClass()).isEqualTo(DeleteClone.class);

        } catch (ParsingException e) {
            e.printStackTrace();
            fail();
        }
    }

}