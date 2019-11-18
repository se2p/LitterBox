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
import scratch.newast.model.expression.string.Str;
import scratch.newast.model.statement.Stmt;
import scratch.newast.model.statement.actorlook.AskAndWait;
import scratch.newast.model.statement.actorlook.ClearGraphicEffects;
import scratch.newast.model.statement.actorlook.SwitchBackdrop;
import scratch.newast.model.statement.spritelook.HideVariable;
import scratch.newast.model.statement.spritelook.ShowVariable;
import scratch.newast.model.statement.termination.StopAll;
import scratch.newast.model.variable.Qualified;
import scratch.newast.parser.ProgramParser;

public class ActorLookStmtParserTest {

    private JsonNode project;

    @Before
    public void setup() {
        String path = "src/test/java/scratch/fixtures/actorLookStmts.json";
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
            Program program = ProgramParser.parseProgram("ActorLookStmts", project);
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

            Truth.assertThat(listOfStmt.get(0).getClass()).isEqualTo(AskAndWait.class);
            Truth.assertThat(listOfStmt.get(1).getClass()).isEqualTo(SwitchBackdrop.class);
            Truth.assertThat(listOfStmt.get(2).getClass()).isEqualTo(ShowVariable.class);
            Truth.assertThat(listOfStmt.get(3).getClass()).isEqualTo(HideVariable.class);
            Truth.assertThat(listOfStmt.get(4).getClass()).isEqualTo(ShowVariable.class);
            Truth.assertThat(listOfStmt.get(5).getClass()).isEqualTo(HideVariable.class);
            Truth.assertThat(listOfStmt.get(6).getClass()).isEqualTo(ClearGraphicEffects.class);
            Truth.assertThat(script.getStmtList().getTerminationStmt().getClass()).isEqualTo(StopAll.class);

        } catch (ParsingException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testAskAndWaitStmt() {
        try {
            Program program = ProgramParser.parseProgram("ActorLookStmts", project);
            ActorDefinitionList list = program.getActorDefinitionList();
            ActorDefinition sprite = list.getDefintions().get(1);

            Script script = sprite.getScripts().getScriptList().get(0);
            List<Stmt> listOfStmt = script.getStmtList().getStmts().getListOfStmt();

            Stmt askAndWaitStmt = listOfStmt.get(0);
            Truth.assertThat(askAndWaitStmt.getClass()).isEqualTo(AskAndWait.class);
            Truth.assertThat(((Str) ((AskAndWait) askAndWaitStmt).getQuestion()).getStr())
                .isEqualTo("What's your name?");

        } catch (ParsingException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testSwitchBackdrop() {
        try {
            Program program = ProgramParser.parseProgram("ActorLookStmts", project);
            ActorDefinitionList list = program.getActorDefinitionList();
            ActorDefinition sprite = list.getDefintions().get(1);

            Script script = sprite.getScripts().getScriptList().get(0);
            List<Stmt> listOfStmt = script.getStmtList().getStmts().getListOfStmt();

            Stmt switchBackropStmt = listOfStmt.get(1);
            Truth.assertThat(switchBackropStmt.getClass()).isEqualTo(SwitchBackdrop.class);
            Truth.assertThat(((WithId) ((SwitchBackdrop) switchBackropStmt).getElementChoice()).getIdent().getValue())
                .isEqualTo(
                    "Baseball 1");

        } catch (ParsingException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testShowHideVar() {
        try {
            Program program = ProgramParser.parseProgram("ActorLookStmts", project);
            ActorDefinitionList list = program.getActorDefinitionList();
            ActorDefinition sprite = list.getDefintions().get(1);

            Script script = sprite.getScripts().getScriptList().get(0);
            List<Stmt> listOfStmt = script.getStmtList().getStmts().getListOfStmt();

            Stmt showVariable = listOfStmt.get(2);
            Truth.assertThat(showVariable.getClass()).isEqualTo(ShowVariable.class);
            Truth.assertThat(((Qualified) ((ShowVariable) showVariable).getVariable()).getFirst().getValue())
                .isEqualTo("Stage");
            Truth.assertThat(((Qualified) ((ShowVariable) showVariable).getVariable()).getSecond().getValue())
                .isEqualTo("my variable");

            Stmt hideVariable = listOfStmt.get(3);
            Truth.assertThat(((Qualified) ((HideVariable) hideVariable).getVariable()).getFirst().getValue())
                .isEqualTo("Stage");
            Truth.assertThat(((Qualified) ((HideVariable) hideVariable).getVariable()).getSecond().getValue())
                .isEqualTo("my variable");

        } catch (ParsingException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testShowHideList() {
        try {
            Program program = ProgramParser.parseProgram("ActorLookStmts", project);
            ActorDefinitionList list = program.getActorDefinitionList();
            ActorDefinition sprite = list.getDefintions().get(1);

            Script script = sprite.getScripts().getScriptList().get(0);
            List<Stmt> listOfStmt = script.getStmtList().getStmts().getListOfStmt();

            Stmt showVariable = listOfStmt.get(4);
            Truth.assertThat(showVariable.getClass()).isEqualTo(ShowVariable.class);
            Truth.assertThat(((Qualified) ((ShowVariable) showVariable).getVariable()).getFirst().getValue())
                .isEqualTo("Stage");
            Truth.assertThat(((Qualified) ((ShowVariable) showVariable).getVariable()).getSecond().getValue())
                .isEqualTo("List");

            Stmt hideVariable = listOfStmt.get(5);
            Truth.assertThat(((Qualified) ((HideVariable) hideVariable).getVariable()).getFirst().getValue())
                .isEqualTo("Stage");
            Truth.assertThat(((Qualified) ((HideVariable) hideVariable).getVariable()).getSecond().getValue())
                .isEqualTo("List");

        } catch (ParsingException e) {
            e.printStackTrace();
            fail();
        }
    }
}