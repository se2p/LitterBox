package scratch.newast.parser.stmt;

import static junit.framework.TestCase.fail;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.truth.Truth;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import scratch.newast.ParsingException;
import scratch.newast.model.ActorDefinition;
import scratch.newast.model.ActorDefinitionList;
import scratch.newast.model.Program;
import scratch.newast.model.Script;
import scratch.newast.model.elementchoice.WithId;
import scratch.newast.model.statement.Stmt;
import scratch.newast.model.statement.actorsound.ClearSoundEffects;
import scratch.newast.model.statement.actorsound.PlaySoundUntilDone;
import scratch.newast.model.statement.actorsound.StartSound;
import scratch.newast.model.statement.actorsound.StopAllSounds;
import scratch.newast.model.statement.termination.StopAll;
import scratch.newast.parser.ProgramParser;

public class ActorSoundStmtParserTest {

    private JsonNode project;

    @Before
    public void setup() {
        String path = "src/test/java/scratch/fixtures/actorSoundStmts.json";
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
            Program program = ProgramParser.parseProgram("ActorSoundStmts", project);
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
            Program program = ProgramParser.parseProgram("ActorLookStmts", project);
            ActorDefinitionList list = program.getActorDefinitionList();
            ActorDefinition sprite = list.getDefintions().get(1);

            Script script = sprite.getScripts().getScriptList().get(0);
            List<Stmt> listOfStmt = script.getStmtList().getStmts().getListOfStmt();

            Truth.assertThat(listOfStmt.get(0).getClass()).isEqualTo(PlaySoundUntilDone.class);
            Truth.assertThat(listOfStmt.get(1).getClass()).isEqualTo(StartSound.class);
            Truth.assertThat(listOfStmt.get(2).getClass()).isEqualTo(ClearSoundEffects.class);
            Truth.assertThat(listOfStmt.get(3).getClass()).isEqualTo(StopAllSounds.class);
            Truth.assertThat(script.getStmtList().getTerminationStmt().getClass()).isEqualTo(StopAll.class);

        } catch (ParsingException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testPlaySoundTilDone() {
        try {
            Program program = ProgramParser.parseProgram("ActorLookStmts", project);
            ActorDefinitionList list = program.getActorDefinitionList();
            ActorDefinition sprite = list.getDefintions().get(1);

            Script script = sprite.getScripts().getScriptList().get(0);
            List<Stmt> listOfStmt = script.getStmtList().getStmts().getListOfStmt();

            Truth.assertThat(listOfStmt.get(0).getClass()).isEqualTo(PlaySoundUntilDone.class);

            PlaySoundUntilDone playSoundUntilDone = (PlaySoundUntilDone) listOfStmt.get(0);
            Truth.assertThat(((WithId) playSoundUntilDone.getElementChoice()).getIdent().getValue()).isEqualTo("Meow");

        } catch (ParsingException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testStartSound() {
        try {
            Program program = ProgramParser.parseProgram("ActorLookStmts", project);
            ActorDefinitionList list = program.getActorDefinitionList();
            ActorDefinition sprite = list.getDefintions().get(1);

            Script script = sprite.getScripts().getScriptList().get(0);
            List<Stmt> listOfStmt = script.getStmtList().getStmts().getListOfStmt();

            Truth.assertThat(listOfStmt.get(0).getClass()).isEqualTo(PlaySoundUntilDone.class);

            StartSound startSound = (StartSound) listOfStmt.get(1);
            Truth.assertThat(((WithId) startSound.getElementChoice()).getIdent().getValue()).isEqualTo("Meow");

        } catch (ParsingException e) {
            e.printStackTrace();
            fail();
        }
    }

}