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
package de.uni_passau.fim.se2.litterbox.scratchblocks;

import de.uni_passau.fim.se2.litterbox.ast.model.Message;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptEntity;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.event.GreenFlag;
import de.uni_passau.fim.se2.litterbox.ast.model.event.KeyPressed;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.AsNumber;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.Current;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.PickRandom;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AttributeOf;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.Costume;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.StringExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.AttributeFromFixed;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.AttributeFromVariable;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.position.RandomPos;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.ExpressionStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.AskAndWait;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.GraphicEffect;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.SetGraphicEffectTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.Broadcast;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitSeconds;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitUntil;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Say;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopAll;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class ScratchBlocksToScratchVisitorTest {

    private ScriptEntity getScript(String scratchBlocksInput) {
        final ScratchBlocksParser parser = new ScratchBlocksParser();
        return parser.parseScript(scratchBlocksInput);
    }

    private StmtList getStmtList(String scratchBlocksInput) {
        return getScript(scratchBlocksInput).getStmtList();
    }

    private Script parseScript(final String scratchBlocksInput) {
        ScriptEntity scriptEntity = getScript(scratchBlocksInput);
        assertInstanceOf(Script.class, scriptEntity);

        return (Script) scriptEntity;
    }

    @Test
    void testSayWithLiteral() {
        StmtList statement = getStmtList("say [ja!]\n");
        assertInstanceOf(Say.class, statement.getStatement(0));
        StringExpr expr = ((Say) statement.getStatement(0)).getString();
        assertInstanceOf(StringLiteral.class, expr);
        Assertions.assertEquals("ja!", ((StringLiteral) expr).getText());
    }

    @Test
    void testExprOrLiteralNum() {
        StmtList statements = getStmtList("move (10) steps\n");
        assertInstanceOf(MoveSteps.class, statements.getStatement(0));
        MoveSteps moveSteps = (MoveSteps) statements.getStatement(0);
        double value = ((NumberLiteral) moveSteps.getSteps()).getValue();
        Assertions.assertEquals(10, value);
    }

    @Test
    void testExprOrLiteralString() {
        StmtList statements = getStmtList("move [a] steps\n");
        assertInstanceOf(MoveSteps.class, statements.getStatement(0));
        MoveSteps moveSteps = (MoveSteps) statements.getStatement(0);
        String literal = ((StringLiteral) ((AsNumber) moveSteps.getSteps()).getOperand1()).getText();
        Assertions.assertEquals("a", literal);
    }

    @Test
    void testExprOrLiteralExpression() {
        StmtList statements = getStmtList("move (\\)\\(\\_\\$\\\\\\\\\\\\a) steps\n");
        assertInstanceOf(MoveSteps.class, statements.getStatement(0));
        MoveSteps moveSteps = (MoveSteps) statements.getStatement(0);
        String variableName = ((Variable) ((AsNumber) moveSteps.getSteps()).getOperand1()).getName().getName();
        Assertions.assertEquals(")(_$\\\\\\a", variableName);
    }

    @Test
    void testPositionFixedRandom() {
        StmtList statements = getStmtList("go to (random position v)\n");
        assertInstanceOf(GoToPos.class, statements.getStatement(0));
        GoToPos goToPos = (GoToPos) statements.getStatement(0);
        assertInstanceOf(RandomPos.class, goToPos.getPosition());
    }

    @Test
    void testTouchingColor() {
        StmtList stmtList = getStmtList("<touching color (#ff00ff)?>\n");
        assertInstanceOf(ExpressionStmt.class, stmtList.getStatement(0));
        assertInstanceOf(SpriteTouchingColor.class, ((ExpressionStmt) stmtList.getStatement(0)).getExpression());
    }

    @Test
    void testColorTouchingColor() {
        StmtList stmtList = getStmtList("<color (#ff00ff) is touching (#ffff00)?>\n");
        assertInstanceOf(ExpressionStmt.class, stmtList.getStatement(0));
        assertInstanceOf(ColorTouchingColor.class, ((ExpressionStmt) stmtList.getStatement(0)).getExpression());
    }

    @Test
    void testKeyPressed() {
        StmtList stmtList = getStmtList("<key (a v) pressed?>\n");
        assertInstanceOf(ExpressionStmt.class, stmtList.getStatement(0));
        assertInstanceOf(IsKeyPressed.class, ((ExpressionStmt) stmtList.getStatement(0)).getExpression());
    }

    @Test
    void testBiggerThan() {
        StmtList stmtList = getStmtList("<(7) > (2)>\n");
        assertInstanceOf(ExpressionStmt.class, stmtList.getStatement(0));
        assertInstanceOf(BiggerThan.class, ((ExpressionStmt) stmtList.getStatement(0)).getExpression());
    }

    @Test
    void testCostumeName() {
        StmtList stmtList = getStmtList("(costume [name v])\n");
        assertInstanceOf(ExpressionStmt.class, stmtList.getStatement(0));
        assertInstanceOf(Costume.class, ((ExpressionStmt) stmtList.getStatement(0)).getExpression());
    }

    @Test
    void testAttributeOf() {
        StmtList stmtList = getStmtList("([costume # v] of (Sprite1 v))\n");
        assertInstanceOf(ExpressionStmt.class, stmtList.getStatement(0));
        assertInstanceOf(AttributeOf.class, ((ExpressionStmt) stmtList.getStatement(0)).getExpression());
        assertInstanceOf(AttributeFromFixed.class, ((AttributeOf) ((ExpressionStmt) stmtList.getStatement(0)).getExpression()).getAttribute());
        stmtList = getStmtList("([var v] of (Sprite1 v))\n");
        assertInstanceOf(ExpressionStmt.class, stmtList.getStatement(0));
        assertInstanceOf(AttributeOf.class, ((ExpressionStmt) stmtList.getStatement(0)).getExpression());
        assertInstanceOf(AttributeFromVariable.class, ((AttributeOf) ((ExpressionStmt) stmtList.getStatement(0)).getExpression()).getAttribute());
    }

    @Test
    void testCurrent() {
        StmtList stmtList = getStmtList("(current [day of the week v])\n");
        assertInstanceOf(ExpressionStmt.class, stmtList.getStatement(0));
        assertInstanceOf(Current.class, ((ExpressionStmt) stmtList.getStatement(0)).getExpression());
        Assertions.assertEquals("dayofweek", ((Current) ((ExpressionStmt) stmtList.getStatement(0)).getExpression()).getTimeComp().getTypeName());
    }

    @Test
    void testRotationStyle() {
        StmtList statements = getStmtList("set rotation style [don't rotate v]\n");
        assertInstanceOf(SetRotationStyle.class, statements.getStatement(0));
        SetRotationStyle rotate = (SetRotationStyle) statements.getStatement(0);
        RotationStyle rotationStyle = rotate.getRotation();
        Assertions.assertEquals("don't rotate", rotationStyle.getTypeName());
    }

    @Test
    void testColorEffect() {
        StmtList statements = getStmtList("set [pixelate v] effect to (2)\n");
        assertInstanceOf(SetGraphicEffectTo.class, statements.getStatement(0));
        SetGraphicEffectTo setGraphic = (SetGraphicEffectTo) statements.getStatement(0);
        GraphicEffect effect = setGraphic.getEffect();
        Assertions.assertEquals("pixelate", effect.getTypeName());
    }

    @Test
    void testStopAll() {
        StmtList statements = getStmtList("stop [all v]\n");
        assertInstanceOf(StopAll.class, statements.getStatement(0));
    }

    @Test
    void testForever() {
        StmtList statements = getStmtList("forever\nstop [all v]\nend\n");
        assertInstanceOf(RepeatForeverStmt.class, statements.getStatement(0));
        RepeatForeverStmt forever = (RepeatForeverStmt) statements.getStatement(0);
        assertInstanceOf(StopAll.class, forever.getStmtList().getStatement(0));
    }

    @Test
    void testBroadcast() {
        StmtList statements = getStmtList("broadcast (message1 v)\n");
        assertInstanceOf(Broadcast.class, statements.getStatement(0));
        Message message = ((Broadcast) statements.getStatement(0)).getMessage();
        assertInstanceOf(Message.class, message);
        Assertions.assertEquals("message1", ((StringLiteral) message.getMessage()).getText());
    }

    @Test
    void testAskAndWait() {
        StmtList statements = getStmtList("ask [What's your name?] and wait\n");
        assertInstanceOf(AskAndWait.class, statements.getStatement(0));
        StringExpr question = ((AskAndWait) statements.getStatement(0)).getQuestion();
        assertInstanceOf(StringExpr.class, question);
        Assertions.assertEquals("What's your name?", ((StringLiteral) question).getText());
    }

    @Test
    void testNegativeNumberLiteral() {
        StmtList statements = getStmtList("go to x: (-1) y: (0)\n");
        assertInstanceOf(GoToPosXY.class, statements.getStatement(0));

        GoToPosXY goTo = (GoToPosXY) statements.getStatement(0);
        assertInstanceOf(NumberLiteral.class, goTo.getX());
        assertEquals(-1, ((NumberLiteral) goTo.getX()).getValue());
    }

    @Test
    void testGreenFlag() {
        ScriptEntity scriptEntity = getScript("when green flag clicked\n");
        assertInstanceOf(Script.class, scriptEntity);

        Script script = (Script) scriptEntity;
        assertInstanceOf(GreenFlag.class, script.getEvent());
    }

    @Test
    void testFullScriptGreenFlag() {
        Script script = parseScript("""
                when green flag clicked
                forever
                next costume
                wait (0.1) seconds
                end
                """);
        assertInstanceOf(GreenFlag.class, script.getEvent());
        assertEquals(1, script.getStmtList().getStmts().size());
    }

    @Test
    void testFullScriptWhenKeyPressed() {
        Script script = parseScript("""
                when [any v] key pressed
                set volume to (0) %
                """);
        assertInstanceOf(KeyPressed.class, script.getEvent());
        assertEquals(1, script.getStmtList().getStmts().size());
    }

    @Test
    void testFullScript1() {
        Script script = parseScript("""
                when [any v] key pressed
                start sound (Rick_Astley_-_Never_Gonna_Give_You_Up_Qoret v)
                show
                forever
                wait (0) seconds
                next costume
                end
                """);
        assertInstanceOf(KeyPressed.class, script.getEvent());
        assertEquals(3, script.getStmtList().getStmts().size());
    }

    @Test
    @Disabled("parser does not terminate in a reasonable amount of time")
    // FIXME: grammar needs to be improved *somehow* to fix this
    void testDeeplyNestedExpression() {
        StmtList stmtList = getStmtList("wait (pick random (1) to ((60)-(((((((((((((((((((INFECTED)/(2))/(2))/(2))/(2))/(2))/(2))/(2))/(2))/(2))/(2))/(2))/(2))/(2))/(2))/(2))/(2))/(2))/(2)))) seconds");
        WaitSeconds wait = (WaitSeconds) stmtList.getStatement(0);
        assertInstanceOf(PickRandom.class, wait.getSeconds());
    }

    @Test
    void testNestedBoolExpr() {
        StmtList stmtList = getStmtList("wait until <<<(mouse x) < (-113)> and <(mouse x) > (-123)>> and <<(mouse y) < (-93)> and <(mouse y) > (-101)>>>\n");
        WaitUntil waitUntil = (WaitUntil) stmtList.getStatement(0);
        assertInstanceOf(And.class, waitUntil.getUntil());
    }
}
