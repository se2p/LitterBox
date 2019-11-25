package scratch.ast.parser;

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
import scratch.ast.model.ActorDefinitionList;
import scratch.ast.model.Program;
import scratch.ast.model.Script;
import scratch.ast.model.expression.bool.ColorTouches;
import scratch.ast.model.expression.bool.Touching;
import scratch.ast.model.expression.color.FromNumber;
import scratch.ast.model.literals.ColorLiteral;
import scratch.ast.model.statement.ExpressionStmt;

class ColorParserTest {

    private static JsonNode project;

    @BeforeAll
    public static void setup() {
        String path = "src/test/fixtures/colorBlocks.json";
        File file = new File(path);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            project = objectMapper.readTree(file);
        } catch (IOException e) {
            fail();
        }
    }

    @Test
    public void testFromNumberInColorTouches() {
        try {
            Program program = ProgramParser.parseProgram("Test", project);
            ActorDefinitionList list = program.getActorDefinitionList();
            Truth.assertThat(list.getDefintions().size()).isEqualTo(2);

            final ActorDefinition first = list.getDefintions().get(1);
            final Script script = first.getScripts().getScriptList().get(0);
            final ExpressionStmt expressionStmt = (ExpressionStmt) script.getStmtList().getStmts().getListOfStmt()
                .get(0);
            Truth.assertThat(expressionStmt.getExpression()).isInstanceOf(ColorTouches.class);
            ColorTouches expression = (ColorTouches) expressionStmt.getExpression();

            Truth.assertThat(expression.getOperand1()).isInstanceOf(FromNumber.class);
            Truth.assertThat(expression.getOperand2()).isInstanceOf(FromNumber.class);
        } catch (ParsingException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testColorLiteral() {
        try {
            Program program = ProgramParser.parseProgram("Test", project);
            ActorDefinitionList list = program.getActorDefinitionList();
            Truth.assertThat(list.getDefintions().size()).isEqualTo(2);

            final ActorDefinition first = list.getDefintions().get(1);
            final Script script = first.getScripts().getScriptList().get(1);
            final ExpressionStmt expressionStmt = (ExpressionStmt) script.getStmtList().getStmts().getListOfStmt()
                .get(0);
            Truth.assertThat(expressionStmt.getExpression()).isInstanceOf(Touching.class);
            Touching expression = (Touching) expressionStmt.getExpression();

            Truth.assertThat(expression.getTouchable()).isInstanceOf(ColorLiteral.class);
        } catch (ParsingException e) {
            e.printStackTrace();
            fail();
        }

    }
}