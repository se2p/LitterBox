package scratch.ast.parser.stmt;

import static junit.framework.TestCase.fail;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.truth.Truth;
import java.io.File;
import java.io.IOException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import scratch.ast.ParsingException;
import scratch.ast.model.ActorDefinition;
import scratch.ast.model.Program;
import scratch.ast.model.Script;
import scratch.ast.model.literals.NumberLiteral;
import scratch.ast.model.literals.StringLiteral;
import scratch.ast.model.statement.Stmt;
import scratch.ast.model.statement.list.AddTo;
import scratch.ast.model.statement.list.DeleteAllOf;
import scratch.ast.model.statement.list.DeleteOf;
import scratch.ast.model.statement.list.InsertAt;
import scratch.ast.model.statement.list.ReplaceItem;
import scratch.ast.model.variable.Qualified;
import scratch.ast.parser.ProgramParser;

class ListStmtParserTest {

    private static JsonNode project;

    @BeforeAll
    public static void setup() {
        String path = "src/test/fixtures/listBlocks.json";
        File file = new File(path);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            project = objectMapper.readTree(file);
        } catch (IOException e) {
            fail();
        }
    }

    @Test
    public void testAddToGlobal() {
        try {
            Program program = ProgramParser.parseProgram("ListExpr", project);
            final ActorDefinition sprite = program.getActorDefinitionList().getDefintions().get(1);
            final Script script = sprite.getScripts().getScriptList().get(0);

            final Stmt stmt = script.getStmtList().getStmts().getListOfStmt().get(0);
            Truth.assertThat(stmt).isInstanceOf(AddTo.class);

            final AddTo addTo = (AddTo) stmt;
            Truth.assertThat(((StringLiteral) addTo.getString()).getText()).isEqualTo("thing");
            Truth.assertThat(((Qualified) addTo.getVariable()).getFirst().getName()).isEqualTo("Stage");
            Truth.assertThat(((Qualified) addTo.getVariable()).getSecond().getName()).isEqualTo("TestList");
        } catch (ParsingException e) {
            fail();
        }
    }

    @Test
    public void testAddToLocal() {
        try {
            Program program = ProgramParser.parseProgram("ListExpr", project);
            final ActorDefinition sprite = program.getActorDefinitionList().getDefintions().get(1);
            final Script script = sprite.getScripts().getScriptList().get(1);

            final Stmt stmt = script.getStmtList().getStmts().getListOfStmt().get(1);
            Truth.assertThat(stmt).isInstanceOf(AddTo.class);

            final AddTo addTo = (AddTo) stmt;
            Truth.assertThat(((StringLiteral) addTo.getString()).getText()).isEqualTo("localThing");
            Truth.assertThat(((Qualified) addTo.getVariable()).getFirst().getName()).isEqualTo("Sprite1");
            Truth.assertThat(((Qualified) addTo.getVariable()).getSecond().getName()).isEqualTo("TestListLocal");
        } catch (ParsingException e) {
            fail();
        }
    }

    @Test
    public void testInsertGlobal() {
        try {
            Program program = ProgramParser.parseProgram("ListExpr", project);
            final ActorDefinition sprite = program.getActorDefinitionList().getDefintions().get(1);
            final Script script = sprite.getScripts().getScriptList().get(0);

            final Stmt stmt = script.getStmtList().getStmts().getListOfStmt().get(3);
            Truth.assertThat(stmt).isInstanceOf(InsertAt.class);

            final InsertAt insertAt = (InsertAt) stmt;
            Truth.assertThat(((StringLiteral) insertAt.getString()).getText()).isEqualTo("thing");
            Truth.assertThat(((Qualified) insertAt.getVariable()).getFirst().getName()).isEqualTo("Stage");
            Truth.assertThat(((Qualified) insertAt.getVariable()).getSecond().getName()).isEqualTo("TestList");
        } catch (ParsingException e) {
            fail();
        }
    }

    @Test
    public void testReplaceItem() {
        try {
            Program program = ProgramParser.parseProgram("ListExpr", project);
            final ActorDefinition sprite = program.getActorDefinitionList().getDefintions().get(1);
            final Script script = sprite.getScripts().getScriptList().get(0);

            final Stmt stmt = script.getStmtList().getStmts().getListOfStmt().get(4);
            Truth.assertThat(stmt).isInstanceOf(ReplaceItem.class);

            final ReplaceItem insertAt = (ReplaceItem) stmt;
            Truth.assertThat(((StringLiteral) insertAt.getString()).getText()).isEqualTo("thing2");
            Truth.assertThat(((NumberLiteral) insertAt.getIndex()).getValue()).isEqualTo(1);
            Truth.assertThat(((Qualified) insertAt.getVariable()).getFirst().getName()).isEqualTo("Stage");
            Truth.assertThat(((Qualified) insertAt.getVariable()).getSecond().getName()).isEqualTo("TestList");
        } catch (ParsingException e) {
            fail();
        }
    }

    @Test
    public void testDeleteOf() {
        try {
            Program program = ProgramParser.parseProgram("ListExpr", project);
            final ActorDefinition sprite = program.getActorDefinitionList().getDefintions().get(1);
            final Script script = sprite.getScripts().getScriptList().get(0);

            final Stmt stmt = script.getStmtList().getStmts().getListOfStmt().get(5);
            Truth.assertThat(stmt).isInstanceOf(DeleteOf.class);

            final DeleteOf deleteOf = (DeleteOf) stmt;
            Truth.assertThat(((NumberLiteral) deleteOf.getNum()).getValue()).isEqualTo(1);
            Truth.assertThat(((Qualified) deleteOf.getVariable()).getFirst().getName()).isEqualTo("Stage");
            Truth.assertThat(((Qualified) deleteOf.getVariable()).getSecond().getName()).isEqualTo("TestList");
        } catch (ParsingException e) {
            fail();
        }
    }

    @Test
    public void testDeleteAll() {
        try {
            Program program = ProgramParser.parseProgram("ListExpr", project);
            final ActorDefinition sprite = program.getActorDefinitionList().getDefintions().get(1);
            final Script script = sprite.getScripts().getScriptList().get(0);

            final Stmt stmt = script.getStmtList().getStmts().getListOfStmt().get(6);
            Truth.assertThat(stmt).isInstanceOf(DeleteAllOf.class);

            final DeleteAllOf deleteAllOf = (DeleteAllOf) stmt;
            Truth.assertThat(((Qualified) deleteAllOf.getVariable()).getFirst().getName()).isEqualTo("Stage");
            Truth.assertThat(((Qualified) deleteAllOf.getVariable()).getSecond().getName()).isEqualTo("TestList");
        } catch (ParsingException e) {
            fail();
        }
    }

}