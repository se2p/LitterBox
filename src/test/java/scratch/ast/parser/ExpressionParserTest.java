/*
 * Copyright (C) 2019 LitterBox contributors
 *
 * This file is part of LitterBox.
 *
 * LitterBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * LitterBox is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LitterBox. If not, see <http://www.gnu.org/licenses/>.
 */
package scratch.ast.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import scratch.ast.ParsingException;
import scratch.ast.model.expression.num.*;
import scratch.ast.model.literals.NumberLiteral;
import utils.JsonParser;

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

    @BeforeAll
    public static void setup() {
        moveStepsScript = JsonParser.getBlocksNodeFromJSON("./src/test/java/scratch/fixtures/movesteps.json");
        allExprTypesScript = JsonParser.getBlocksNodeFromJSON("./src/test/java/scratch/fixtures/allexprtypes.json");
        twoNumExprSlotsNumExprs = JsonParser.getBlocksNodeFromJSON("./src/test/java/scratch/fixtures/twoNumExprSlotsNumExprs.json");
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
        NumberLiteral result = NumExprParser.parseNumber(inputs, 0);
        assertEquals("10.0", String.valueOf(result.getValue()));
    }

    @Test
    public void testParseNumExprLiteral() throws ParsingException {
        NumExpr numExpr = NumExprParser.parseNumExpr(literalBlock, 0, allExprTypesScript);
        assertTrue(numExpr instanceof NumberLiteral);
    }

    @Test
    public void testParseNumExprBlock() throws ParsingException {
        NumExpr numExpr = NumExprParser.parseNumExpr(blockBlock, 0, allExprTypesScript);
        assertTrue(numExpr instanceof MouseX);
    }

    @Test
    public void testAdd() throws ParsingException {
        NumExpr add = NumExprParser.parseNumExpr(addBlock, 0, twoNumExprSlotsNumExprs);
        assertTrue(add instanceof Add);
        assertEquals("1.0", String.valueOf(((NumberLiteral) ((Add) add).getOperand1()).getValue()));
        assertEquals("2.0", String.valueOf(((NumberLiteral) ((Add) add).getOperand2()).getValue()));
    }

    @Test
    public void testMinus() throws ParsingException {
        NumExpr minus = NumExprParser.parseNumExpr(minusBlock, 0, twoNumExprSlotsNumExprs);
        assertTrue(minus instanceof Minus);
        assertEquals("1.0", String.valueOf(((NumberLiteral) ((Minus) minus).getOperand1()).getValue()));
        assertEquals("2.0", String.valueOf(((NumberLiteral) ((Minus) minus).getOperand2()).getValue()));
    }

    @Test
    public void testMult() throws ParsingException {
        NumExpr mult = NumExprParser.parseNumExpr(multBlock, 0, twoNumExprSlotsNumExprs);
        assertTrue(mult instanceof Mult);
        assertEquals("1.0", String.valueOf(((NumberLiteral) ((Mult) mult).getOperand1()).getValue()));
        assertEquals("2.0", String.valueOf(((NumberLiteral) ((Mult) mult).getOperand2()).getValue()));
    }

    @Test
    public void testDiv() throws ParsingException {
        NumExpr div = NumExprParser.parseNumExpr(divBlock, 0, twoNumExprSlotsNumExprs);
        assertTrue(div instanceof Div);
        PickRandom pickRandom = (PickRandom) ((Div) div).getOperand1();
        assertEquals("1.0", String.valueOf(((NumberLiteral) (pickRandom.getFrom())).getValue()));
        assertEquals("2.0", String.valueOf(((NumberLiteral) (pickRandom.getTo())).getValue()));
        Mod mod = (Mod) ((Div) div).getOperand2();
        assertEquals("1.0", String.valueOf(((NumberLiteral) (mod.getOperand1())).getValue()));
        assertEquals("2.0", String.valueOf(((NumberLiteral) (mod.getOperand2())).getValue()));
    }

    @Test
    public void testNumFuncts() throws ParsingException {
        JsonNode script = JsonParser.getBlocksNodeFromJSON("./src/test/java/scratch/fixtures/numfuncts.json");
        JsonNode pow10Block = script.get("xbBc!xS=1Yz2Yp/DF;JT");
        assertTrue(NumExprParser.parseNumFunct(pow10Block.get("fields")) instanceof NumFunct);

        assertTrue((NumExprParser.parseNumFunct(pow10Block.get("fields"))).equals(NumFunct.POW10));
    }
}
