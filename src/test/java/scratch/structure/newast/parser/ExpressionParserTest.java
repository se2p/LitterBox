package scratch.structure.newast.parser;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Before;
import org.junit.Test;
import scratch.newast.ParsingException;
import scratch.newast.model.expression.num.Add;
import scratch.newast.model.expression.num.Div;
import scratch.newast.model.expression.num.Minus;
import scratch.newast.model.expression.num.Mod;
import scratch.newast.model.expression.num.MouseX;
import scratch.newast.model.expression.num.Mult;
import scratch.newast.model.expression.num.NumExpr;
import scratch.newast.model.expression.num.Number;
import scratch.newast.model.expression.num.PickRandom;
import scratch.newast.model.numfunct.Pow10;
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

    private static JsonNode twoNumExprSlotsNumExprs;
    private static JsonNode addBlock;
    private static JsonNode minusBlock;
    private static JsonNode divBlock;
    private static JsonNode multBlock;

    @Before
    public void setup() {
        moveStepsScript = JsonParser.getBlocksNodeFromJSON("./src/test/java/scratch/structure/ast/fixtures/movesteps.json");
        allExprTypesScript = JsonParser.getBlocksNodeFromJSON("./src/test/java/scratch/structure/ast/fixtures/allexprtypes.json");
        twoNumExprSlotsNumExprs = JsonParser.getBlocksNodeFromJSON("./src/test/java/scratch/structure/ast/fixtures/twoNumExprSlotsNumExprs.json");
        literalBlock = allExprTypesScript.get("QJ:02/{CIWEai#dfuC(k");
        variableBlock = allExprTypesScript.get("Q0r@4R,=K;bq+x;8?O)j");
        listBlock = allExprTypesScript.get("3k1#g23nWs5dk)w3($|+");
        blockBlock = allExprTypesScript.get("K0-dZ/kW=hWWb/GpMt8:");

        addBlock = twoNumExprSlotsNumExprs.get("$`zwlVu=MrX}[7_|OkP0");
        minusBlock = twoNumExprSlotsNumExprs.get("kNxFx|sm51cAUYf?x(cR");
        divBlock = twoNumExprSlotsNumExprs.get("b2JumU`zm:?3szh/07O(");
        multBlock = twoNumExprSlotsNumExprs.get("IBYSC9r)0ccPx;?l-2M|");
    }

    @Test
    public void testParseNumber() {
        JsonNode inputs = moveStepsScript.get("EU(l=G6)z8NGlJFcx|fS").get("inputs");
        Number result = ExpressionParser.parseNumber(inputs, 0);
        assertEquals("10.0", String.valueOf(result.getValue()));
    }

    @Test
    public void testParseNumExprLiteral() throws ParsingException {
        NumExpr numExpr = ExpressionParser.parseNumExpr(literalBlock, 0, allExprTypesScript);
        assertTrue(numExpr instanceof Number);
    }

    @Test
    public void testParseNumExprVar() throws ParsingException {
        NumExpr numExpr = ExpressionParser.parseNumExpr(variableBlock, 0, allExprTypesScript);
        assertNull(numExpr);
    }

    @Test
    public void testParseNumExprList() throws ParsingException {
        NumExpr numExpr = ExpressionParser.parseNumExpr(listBlock, 0, allExprTypesScript);
        assertNull(numExpr);
    }

    @Test
    public void testParseNumExprBlock() throws ParsingException {
        NumExpr numExpr = ExpressionParser.parseNumExpr(blockBlock, 0, allExprTypesScript);
        assertTrue(numExpr instanceof MouseX);
    }

    @Test
    public void testAdd() throws ParsingException {
        NumExpr add = ExpressionParser.parseNumExpr(addBlock, 0, twoNumExprSlotsNumExprs);
        assertTrue(add instanceof Add);
        assertEquals("1.0", String.valueOf(((Number) ((Add) add).getFirst()).getValue()));
        assertEquals("2.0", String.valueOf(((Number) ((Add) add).getSecond()).getValue()));
    }

    @Test
    public void testMinus() throws ParsingException {
        NumExpr minus = ExpressionParser.parseNumExpr(minusBlock, 0, twoNumExprSlotsNumExprs);
        assertTrue(minus instanceof Minus);
        assertEquals("1.0", String.valueOf(((Number) ((Minus) minus).getFirst()).getValue()));
        assertEquals("2.0", String.valueOf(((Number) ((Minus) minus).getSecond()).getValue()));
    }

    @Test
    public void testMult() throws ParsingException {
        NumExpr mult = ExpressionParser.parseNumExpr(multBlock, 0, twoNumExprSlotsNumExprs);
        assertTrue(mult instanceof Mult);
        assertEquals("1.0", String.valueOf(((Number) ((Mult) mult).getFirst()).getValue()));
        assertEquals("2.0", String.valueOf(((Number) ((Mult) mult).getSecond()).getValue()));
    }

    @Test
    public void testDiv() throws ParsingException {
        NumExpr div = ExpressionParser.parseNumExpr(divBlock, 0, twoNumExprSlotsNumExprs);
        assertTrue(div instanceof Div);
        PickRandom pickRandom = (PickRandom) ((Div) div).getFirst();
        assertEquals("1.0", String.valueOf(((Number) (pickRandom.getFrom())).getValue()));
        assertEquals("2.0", String.valueOf(((Number) (pickRandom.getTo())).getValue()));
        Mod mod = (Mod) ((Div) div).getSecond();
        assertEquals("1.0", String.valueOf(((Number) (mod.getFirst())).getValue()));
        assertEquals("2.0", String.valueOf(((Number) (mod.getSecond())).getValue()));
    }

    @Test
    public void testNumFuncts() throws ParsingException {
        JsonNode script = JsonParser.getBlocksNodeFromJSON("./src/test/java/scratch/structure/ast/fixtures/numfuncts.json");
        JsonNode pow10Block = script.get("xbBc!xS=1Yz2Yp/DF;JT");
        assertTrue(ExpressionParser.parseNumFunct(pow10Block.get("fields")) instanceof Pow10);
    }


}
