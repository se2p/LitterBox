package scratch.structure.newast.parser;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Before;
import org.junit.Test;
import scratch.newast.ParsingException;
import scratch.newast.model.expression.num.MouseX;
import scratch.newast.model.expression.num.NumExpr;
import scratch.newast.model.expression.num.Number;
import scratch.newast.parser.ExpressionParser;
import utils.JsonParser;

import static org.junit.Assert.*;
import static scratch.newast.Constants.INPUTS_KEY;

public class ExpressionParserTest {
    private static JsonNode moveStepsScript;
    private static JsonNode allExprTypesScript;
    private static JsonNode literalBlock;
    private static JsonNode variableBlock;
    private static JsonNode listBlock;
    private static JsonNode blockBlock;

    @Before
    public void setup() {
        moveStepsScript = JsonParser.getBlocksNodeFromJSON("./src/test/java/scratch/structure/ast/fixtures/movesteps.json");
        allExprTypesScript = JsonParser.getBlocksNodeFromJSON("./src/test/java/scratch/structure/ast/fixtures/allexprtypes.json");
        literalBlock = allExprTypesScript.get("QJ:02/{CIWEai#dfuC(k");
        variableBlock = allExprTypesScript.get("Q0r@4R,=K;bq+x;8?O)j");
        listBlock = allExprTypesScript.get("3k1#g23nWs5dk)w3($|+");
        blockBlock = allExprTypesScript.get("K0-dZ/kW=hWWb/GpMt8:");
    }

    @Test
    public void testParseNumber() {
        JsonNode inputs = moveStepsScript.get("EU(l=G6)z8NGlJFcx|fS").get("inputs");
        Number result = ExpressionParser.parseNumber(inputs, 0);
        assertEquals("10.0", String.valueOf(result.getValue()));
    }

    @Test
    public void testParseNumExprLiteral() throws ParsingException {
        NumExpr numExpr = ExpressionParser.parseNumExpr(literalBlock.get(INPUTS_KEY), 0, allExprTypesScript);
        assertTrue(numExpr instanceof Number);
    }

    @Test
    public void testParseNumExprVar() throws ParsingException {
        NumExpr numExpr = ExpressionParser.parseNumExpr(variableBlock.get(INPUTS_KEY), 0, allExprTypesScript);
        assertNull(numExpr);
    }

    @Test
    public void testParseNumExprList() throws ParsingException {
        NumExpr numExpr = ExpressionParser.parseNumExpr(listBlock.get(INPUTS_KEY), 0, allExprTypesScript);
        assertNull(numExpr);
    }

    @Test
    public void testParseNumExprBlock() throws ParsingException {
        NumExpr numExpr = ExpressionParser.parseNumExpr(blockBlock.get(INPUTS_KEY), 0, allExprTypesScript);
        assertTrue(numExpr instanceof MouseX);
    }



}
