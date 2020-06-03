package de.uni_passau.fim.se2.litterbox.ast.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.truth.Truth;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.WithExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AttributeOf;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.ExpressionStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static junit.framework.TestCase.fail;

public class AttributeOfExpressionTest {
    private static JsonNode project;

    @BeforeAll
    public static void setup() {
        String path = "src/test/fixtures/attributeOfExpression.json";
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
        Program program = ProgramParser.parseProgram("AttributeOf", project);
        final ActorDefinition sprite = program.getActorDefinitionList().getDefintions().get(0);
        final Script script = sprite.getScripts().getScriptList().get(0);

        final Stmt stmt = script.getStmtList().getStmts().get(0);
        Truth.assertThat(stmt).isInstanceOf(ExpressionStmt.class);

        ExpressionStmt expressionStmt = (ExpressionStmt) stmt;
        Truth.assertThat(expressionStmt.getExpression()).isInstanceOf(AttributeOf.class);
        AttributeOf attributeOf = (AttributeOf) expressionStmt.getExpression();
        Truth.assertThat(attributeOf.getElementChoice()).isInstanceOf(WithExpr.class);
    }
}
