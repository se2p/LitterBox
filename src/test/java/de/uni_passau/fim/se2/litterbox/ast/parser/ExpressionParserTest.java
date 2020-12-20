/*
 * Copyright (C) 2020 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.ast.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.ItemOfVariable;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.UnspecifiedId;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.ExpressionStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.utils.JsonParser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.STEPS_KEY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ExpressionParserTest {

    private static JsonNode moveStepsScript;
    private static JsonNode allExprTypesScript;
    private static JsonNode literalBlock;
    private static JsonNode containingBlock;

    private static JsonNode twoNumExprSlotsNumExprs;
    private static JsonNode addBlock;
    private static JsonNode minusBlock;
    private static JsonNode divBlock;
    private static JsonNode multBlock;

    @BeforeAll
    public static void setup() throws IOException {
        moveStepsScript = JsonParser.getBlocksNodeFromJSON("./src/test/fixtures/movesteps.json");
        allExprTypesScript = JsonParser.getBlocksNodeFromJSON("./src/test/fixtures/allexprtypes.json");
        twoNumExprSlotsNumExprs = JsonParser.getBlocksNodeFromJSON("./src/test/fixtures/twoNumExprSlotsNumExprs.json");
        literalBlock = allExprTypesScript.get("QJ:02/{CIWEai#dfuC(k");
        containingBlock = allExprTypesScript.get("K0-dZ/kW=hWWb/GpMt8:");

        addBlock = twoNumExprSlotsNumExprs.get("$`zwlVu=MrX}[7_|OkP0");
        minusBlock = twoNumExprSlotsNumExprs.get("kNxFx|sm51cAUYf?x(cR");
        divBlock = twoNumExprSlotsNumExprs.get("b2JumU`zm:?3szh/07O(");
        multBlock = twoNumExprSlotsNumExprs.get("IBYSC9r)0ccPx;?l-2M|");
    }

    @Test
    public void testParseNumber() throws ParsingException {
        JsonNode inputs = moveStepsScript.get("EU(l=G6)z8NGlJFcx|fS").get("inputs");
        NumberLiteral result = NumExprParser.parseNumber(inputs, STEPS_KEY);
        assertEquals("10.0", String.valueOf(result.getValue()));
    }

    @Test
    public void testParseNumExprLiteral() throws ParsingException {
        NumExpr numExpr = NumExprParser.parseNumExpr(literalBlock, STEPS_KEY, allExprTypesScript);
        assertTrue(numExpr instanceof NumberLiteral);
    }

    @Test
    public void testParseNumExprBlock() throws ParsingException {
        NumExpr numExpr = NumExprParser.parseNumExpr(containingBlock, STEPS_KEY, allExprTypesScript);
        assertTrue(numExpr instanceof MouseX);
    }

    @Test
    public void testAdd() throws ParsingException {
        NumExpr add = NumExprParser.parseNumExpr(addBlock, STEPS_KEY, twoNumExprSlotsNumExprs);
        assertTrue(add instanceof Add);
        assertEquals("1.0", String.valueOf(((NumberLiteral) ((Add) add).getOperand1()).getValue()));
        assertEquals("2.0", String.valueOf(((NumberLiteral) ((Add) add).getOperand2()).getValue()));
    }

    @Test
    public void testMinus() throws ParsingException {
        NumExpr minus = NumExprParser.parseNumExpr(minusBlock, STEPS_KEY, twoNumExprSlotsNumExprs);
        assertTrue(minus instanceof Minus);
        assertEquals("1.0", String.valueOf(((NumberLiteral) ((Minus) minus).getOperand1()).getValue()));
        assertEquals("2.0", String.valueOf(((NumberLiteral) ((Minus) minus).getOperand2()).getValue()));
    }

    @Test
    public void testMult() throws ParsingException {
        NumExpr mult = NumExprParser.parseNumExpr(multBlock, STEPS_KEY, twoNumExprSlotsNumExprs);
        assertTrue(mult instanceof Mult);
        assertEquals("1.0", String.valueOf(((NumberLiteral) ((Mult) mult).getOperand1()).getValue()));
        assertEquals("2.0", String.valueOf(((NumberLiteral) ((Mult) mult).getOperand2()).getValue()));
    }

    @Test
    public void testDiv() throws ParsingException {
        NumExpr div = NumExprParser.parseNumExpr(divBlock, STEPS_KEY, twoNumExprSlotsNumExprs);
        assertTrue(div instanceof Div);
        PickRandom pickRandom = (PickRandom) ((Div) div).getOperand1();
        assertEquals("1.0", String.valueOf(((NumberLiteral) (pickRandom.getOperand1())).getValue()));
        assertEquals("2.0", String.valueOf(((NumberLiteral) (pickRandom.getOperand2())).getValue()));
        Mod mod = (Mod) ((Div) div).getOperand2();
        assertEquals("1.0", String.valueOf(((NumberLiteral) (mod.getOperand1())).getValue()));
        assertEquals("2.0", String.valueOf(((NumberLiteral) (mod.getOperand2())).getValue()));
    }

    @Test
    public void testNumFuncts() throws IOException {
        JsonNode script = JsonParser.getBlocksNodeFromJSON("./src/test/fixtures/numfuncts.json");
        JsonNode pow10Block = script.get("xbBc!xS=1Yz2Yp/DF;JT");
        assertTrue(NumExprParser.parseNumFunct(pow10Block.get("fields")) instanceof NumFunct);

        assertTrue((NumExprParser.parseNumFunct(pow10Block.get("fields"))).getType().equals(NumFunct.NumFunctType.POW10));
    }

    @Test
    public void testStatement() throws IOException {
        JsonNode script = JsonParser.getBlocksNodeFromJSON("./src/test/fixtures/bugpattern/missingLoopSensingMultiple.json");
        JsonNode ifBlock = script.get(".-Id3Zrhoe,6;Z+v_;IB");

        Exception exception = assertThrows(ParsingException.class, () -> {
            ExpressionParser.parseExprBlock(".-Id3Zrhoe,6;Z+v_;IB", ifBlock, script);
        });
        String expectedMessage = " is an unexpected opcode for an expression";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void testItemOfListWithoutListId() throws Exception {
        Program program = ProgramParser.parseProgram("Test", new ObjectMapper().readTree(new File("src/test/fixtures/missingListIdWithItemOf.json")));
        List<ActorDefinition> definitions = program.getActorDefinitionList().getDefinitions();
        Script script = definitions.get(1).getScripts().getScriptList().get(0);
        List<Stmt> stmts = script.getStmtList().getStmts();
        Stmt exprStmt = stmts.get(0);
        Expression expr = ((ExpressionStmt) exprStmt).getExpression();
        assertTrue(((ItemOfVariable) expr).getIdentifier() instanceof UnspecifiedId);
    }
}
