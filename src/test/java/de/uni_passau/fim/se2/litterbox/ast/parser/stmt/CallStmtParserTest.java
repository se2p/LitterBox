package de.uni_passau.fim.se2.litterbox.ast.parser.stmt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.ComparableExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BiggerThan;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.Volume;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.Join;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.CallStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;

class CallStmtParserTest {

    private static JsonNode project;

    @BeforeAll
    public static void setup() {
        String path = "src/test/fixtures/stmtParser/procCallInputs.json";
        File file = new File(path);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            project = objectMapper.readTree(file);
        } catch (IOException e) {
            fail();
        }
    }

    @Test
    public void testInputParsing() {
        try {
            Program program = ProgramParser.parseProgram("CommonStmts", project);
            ActorDefinitionList actorDefinitionList = program.getActorDefinitionList();
            for (ActorDefinition definition : actorDefinitionList.getDefintions()) {
                if (definition.getActorType().equals(ActorType.SPRITE)) {
                    List<Script> scriptList = definition.getScripts().getScriptList();
                    Script script = scriptList.get(0);
                    Stmt stmt = script.getStmtList().getStmts().get(0);
                    assertTrue(stmt instanceof CallStmt);
                    CallStmt callStmt = (CallStmt) stmt;
                    List<Expression> expressions = callStmt.getExpressions().getExpressions();
                    assertTrue(expressions.get(0) instanceof Volume);
                    Expression biggerThan = expressions.get(1);
                    assertTrue(biggerThan instanceof BiggerThan);
                    ComparableExpr operand1 = ((BiggerThan) biggerThan).getOperand1();
                    ComparableExpr operand2 = ((BiggerThan) biggerThan).getOperand2();
                    assertTrue(operand1 instanceof Join);
                    assertTrue(operand2 instanceof NumberLiteral);
                }
            }
        } catch (ParsingException e) {
            e.printStackTrace();
            fail();
        }
    }
}