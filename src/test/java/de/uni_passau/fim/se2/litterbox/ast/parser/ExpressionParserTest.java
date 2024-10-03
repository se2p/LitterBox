/*
 * Copyright (C) 2019-2024 LitterBox contributors
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

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.*;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.ExpressionStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.MoveSteps;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static de.uni_passau.fim.se2.litterbox.JsonTest.getBlock;

public class ExpressionParserTest implements JsonTest {

    private static Program moveStepsScript;
    private static MoveSteps containingBlock;

    private static MoveSteps addBlock;
    private static MoveSteps minusBlock;
    private static MoveSteps divBlock;
    private static MoveSteps multBlock;

    @BeforeAll
    public static void setup() throws IOException, ParsingException {
        moveStepsScript = JsonTest.parseProgram("./src/test/fixtures/movesteps.json");
        Program allExprTypesScript = JsonTest.parseProgram("./src/test/fixtures/allexprtypes.json");
        Program twoNumExprSlotsNumExprs = JsonTest.parseProgram("./src/test/fixtures/twoNumExprSlotsNumExprs.json");
        containingBlock = getBlock(allExprTypesScript, "K0-dZ/kW=hWWb/GpMt8:", MoveSteps.class);

        addBlock = getBlock(twoNumExprSlotsNumExprs, "$`zwlVu=MrX}[7_|OkP0", MoveSteps.class);
        minusBlock = getBlock(twoNumExprSlotsNumExprs, "kNxFx|sm51cAUYf?x(cR", MoveSteps.class);
        divBlock = getBlock(twoNumExprSlotsNumExprs, "b2JumU`zm:?3szh/07O(", MoveSteps.class);
        multBlock = getBlock(twoNumExprSlotsNumExprs, "IBYSC9r)0ccPx;?l-2M|", MoveSteps.class);
    }

    @Test
    public void testParseNumber() {
        MoveSteps moveSteps = getBlock(moveStepsScript, "EU(l=G6)z8NGlJFcx|fS", MoveSteps.class);
        assertEquals(new NumberLiteral(10.0), moveSteps.getSteps());
    }

    @Test
    public void testParseNumExprBlock() {
        assertInstanceOf(MouseX.class, containingBlock.getSteps());
    }

    @Test
    public void testAdd() {
        NumExpr add = addBlock.getSteps();
        assertInstanceOf(Add.class, add);
        assertEquals("1.0", String.valueOf(((NumberLiteral) ((Add) add).getOperand1()).getValue()));
        assertEquals("2.0", String.valueOf(((NumberLiteral) ((Add) add).getOperand2()).getValue()));
    }

    @Test
    public void testMinus() {
        NumExpr minus = minusBlock.getSteps();
        assertInstanceOf(Minus.class, minus);
        assertEquals("1.0", String.valueOf(((NumberLiteral) ((Minus) minus).getOperand1()).getValue()));
        assertEquals("2.0", String.valueOf(((NumberLiteral) ((Minus) minus).getOperand2()).getValue()));
    }

    @Test
    public void testMult() {
        NumExpr mult = multBlock.getSteps();
        assertInstanceOf(Mult.class, mult);
        assertEquals("1.0", String.valueOf(((NumberLiteral) ((Mult) mult).getOperand1()).getValue()));
        assertEquals("2.0", String.valueOf(((NumberLiteral) ((Mult) mult).getOperand2()).getValue()));
    }

    @Test
    public void testDiv() {
        NumExpr div = divBlock.getSteps();
        assertInstanceOf(Div.class, div);

        PickRandom pickRandom = (PickRandom) ((Div) div).getOperand1();
        assertEquals("1.0", String.valueOf(((NumberLiteral) (pickRandom.getOperand1())).getValue()));
        assertEquals("2.0", String.valueOf(((NumberLiteral) (pickRandom.getOperand2())).getValue()));
        Mod mod = (Mod) ((Div) div).getOperand2();
        assertEquals("1.0", String.valueOf(((NumberLiteral) (mod.getOperand1())).getValue()));
        assertEquals("2.0", String.valueOf(((NumberLiteral) (mod.getOperand2())).getValue()));
    }

    @Test
    public void testNumFuncts() throws IOException, ParsingException {
        Program program = JsonTest.parseProgram("./src/test/fixtures/numfuncts.json");
        ExpressionStmt stmt = getBlock(program, "xbBc!xS=1Yz2Yp/DF;JT", ExpressionStmt.class);
        NumFunctOf pow10Block = (NumFunctOf) stmt.getExpression();

        assertEquals(NumFunct.NumFunctType.POW10, pow10Block.getOperand1().getType());
    }
}
