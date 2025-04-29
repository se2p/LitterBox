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

import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.event.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.BinaryExpression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.AttributeFromFixed;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.AttributeFromVariable;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.FixedAttribute;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.ColorLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.TopNonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.position.RandomPos;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ParameterDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.CallStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.ExpressionStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.AskAndWait;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.ChangeGraphicEffectBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.GraphicEffect;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.SetGraphicEffectTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.Broadcast;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitSeconds;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitUntil;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatTimesStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.list.AddTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.GoToLayer;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.LayerChoice;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Say;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopAll;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.color.FromNumber;
import de.uni_passau.fim.se2.litterbox.ast.model.type.BooleanType;
import de.uni_passau.fim.se2.litterbox.ast.model.type.StringType;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.ScratchList;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ScratchBlocksToScratchVisitorTest {

    private ScriptEntity getScript(String scratchBlocksInput) {
        final ScratchBlocksParser parser = new ScratchBlocksParser();
        if (!scratchBlocksInput.endsWith("\n")) {
            scratchBlocksInput += "\n";
        }
        return parser.parseScript(scratchBlocksInput);
    }

    private ScriptList getScriptList(String scratchBlocksInput) {
        final ScratchBlocksParser parser = new ScratchBlocksParser();
        if (!scratchBlocksInput.endsWith("\n")) {
            scratchBlocksInput += "\n";
        }
        return parser.parseScriptList(scratchBlocksInput);
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
        assertEquals("ja!", ((StringLiteral) expr).getText());
    }

    @Test
    void testExprOrLiteralNum() {
        StmtList statements = getStmtList("move (10) steps\n");
        assertInstanceOf(MoveSteps.class, statements.getStatement(0));
        MoveSteps moveSteps = (MoveSteps) statements.getStatement(0);
        double value = ((NumberLiteral) moveSteps.getSteps()).getValue();
        assertEquals(10, value);
    }

    @Test
    void testExprOrLiteralString() {
        StmtList statements = getStmtList("move [a] steps\n");
        assertInstanceOf(MoveSteps.class, statements.getStatement(0));
        MoveSteps moveSteps = (MoveSteps) statements.getStatement(0);
        String literal = ((StringLiteral) ((AsNumber) moveSteps.getSteps()).getOperand1()).getText();
        assertEquals("a", literal);
    }

    @ParameterizedTest
    @MethodSource("variables")
    void testVariableExpression(final String input, final String expectedVariableName) {
        final StmtList stmtList = getStmtList(input);
        final Expression expr = ((ExpressionStmt) stmtList.getStatement(0)).getExpression();

        assertInstanceOf(Qualified.class, expr);
        assertEquals(expectedVariableName, ((Qualified) expr).getSecond().getName().getName());
    }

    static Stream<Arguments> variables() {
        return Stream.of(
                Arguments.of("(x)", "x"),
                Arguments.of("(x\\()", "x("),
                Arguments.of("(x\\)abc)", "x)abc"),
                Arguments.of("(abc9de)", "abc9de"),
                Arguments.of("(\\)\\(\\_\\$\\\\\\\\\\\\\\a)", ")(_$\\\\\\a")
        );
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
        assertHasExprStmt(stmtList, SpriteTouchingColor.class);
    }

    @Test
    void testColorTouchingColor() {
        StmtList stmtList = getStmtList("<color (#ff00ff) is touching (#ffff00)?>\n");
        assertHasExprStmt(stmtList, ColorTouchingColor.class);
    }

    @Test
    void testKeyPressed() {
        StmtList stmtList = getStmtList("<key (a v) pressed?>\n");
        assertHasExprStmt(stmtList, IsKeyPressed.class);
    }

    @Test
    void testBiggerThan() {
        StmtList stmtList = getStmtList("<(7) > (2)>\n");
        assertHasExprStmt(stmtList, BiggerThan.class);
    }

    @ParameterizedTest
    @MethodSource("binaryNumberOperators")
    <T extends Expression> void testBinaryNumberOperatorSpaced(final String operator, final Class<T> expressionType) {
        final String expr = String.format("((7) %s (2))", operator);
        StmtList stmtList = getStmtList(expr);
        assertHasExprStmt(stmtList, expressionType);
    }

    @ParameterizedTest
    @MethodSource("binaryNumberOperators")
    <T extends Expression> void testBinaryNumberOperatorNoSpaces(final String operator, final Class<T> expressionType) {
        final String expr = String.format("((4)%s(1))", operator);

        final StmtList stmtList = getStmtList(expr);
        assertHasExprStmt(stmtList, expressionType);
    }

    @ParameterizedTest
    @MethodSource("binaryBoolOperators")
    <T extends Expression> void testBinaryBoolOperatorSpaced(final String operator, final Class<T> expressionType) {
        final String expr = String.format("<<mouse down?> %s <mouse down?>>", operator);
        StmtList stmtList = getStmtList(expr);
        assertHasExprStmt(stmtList, expressionType);
    }

    /*
     * Binary expressions without spaces around the operator cannot be parsed.
     */
    @ParameterizedTest
    @MethodSource("binaryBoolOperators")
    <T extends Expression> void testBinaryBoolOperatorNoSpaces(final String operator, final Class<T> expressionType) {
        final String expr = String.format("<<mouse down?>%s<mouse down?>>", operator);
        assertHasExprStmt(getStmtList(expr), expressionType);
    }

    @ParameterizedTest
    @MethodSource("binaryComparisonOperators")
    <T extends Expression> void testBinaryComparisonOperatorSpaced(final String operator, final Class<T> expressionType) {
        final String expr = String.format("<(3) %s (90)>", operator);
        StmtList stmtList = getStmtList(expr);
        assertHasExprStmt(stmtList, expressionType);
    }

    /*
     * Binary expressions without spaces around the operator cannot be parsed.
     */
    @ParameterizedTest
    @MethodSource("binaryComparisonOperators")
    <T extends Expression> void testBinaryComparisonOperatorNoSpaces(final String operator, final Class<T> expressionType) {
        final String expr = String.format("<(3)%s(4)>", operator);

        if ("=".equals(operator)) {
            assertHasExprStmt(getStmtList(expr), expressionType);
        } else {
            // fixme: this should probably not be parsed at all
            assertStatementType(expr, CallStmt.class);
        }
    }

    /*
     * Boolean expressions can be dragged into the round elements of comparison operators.
     */
    @ParameterizedTest
    @MethodSource("binaryComparisonOperators")
    <T extends Expression> void testComparisonAsStringConversion(final String operator, final Class<T> expressionType) {
        final String expr = String.format("<(level) %s <(level) < (20)>>", operator);
        final T op = assertHasExprStmt(getStmtList(expr), expressionType);

        assertInstanceOf(BinaryExpression.class, op);
        assertInstanceOf(AsString.class, ((BinaryExpression<?, ?>) op).getOperand2());
    }

    @Test
    void testAndExprLhsOfEquals() {
        final String expr = "<<<(time) > (1.1)> and <(time) < (1.9)>> = <(backdrop [number v]) = (1)>>";
        final Equals eq = assertHasExprStmt(getStmtList(expr), Equals.class);

        assertInstanceOf(AsString.class, eq.getOperand1());
    }

    static Stream<Arguments> binaryNumberOperators() {
        return Stream.of(
                Arguments.of("+", Add.class),
                Arguments.of("-", Minus.class),
                Arguments.of("*", Mult.class),
                Arguments.of("/", Div.class),
                Arguments.of("mod", Mod.class)
        );
    }

    static Stream<Arguments> binaryBoolOperators() {
        return Stream.of(
                Arguments.of("and", And.class),
                Arguments.of("or", Or.class)
        );
    }

    static Stream<Arguments> binaryComparisonOperators() {
        return Stream.of(
                Arguments.of(">", BiggerThan.class),
                Arguments.of("<", LessThan.class),
                Arguments.of("=", Equals.class)
        );
    }

    @Test
    void testTouchingCastColor() {
        final String expr = "<color [#000000] is touching <(123) = (50)> ?>";
        final ColorTouchingColor ctc = assertHasExprStmt(getStmtList(expr), ColorTouchingColor.class);

        assertInstanceOf(ColorLiteral.class, ctc.getOperand1());

        assertInstanceOf(FromNumber.class, ctc.getOperand2());
        final FromNumber rhs = (FromNumber) ctc.getOperand2();
        assertInstanceOf(AsNumber.class, rhs.getValue());
        final AsNumber numExpr = (AsNumber) rhs.getValue();
        assertInstanceOf(Equals.class, numExpr.getOperand1());
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
        assertEquals("dayofweek", ((Current) ((ExpressionStmt) stmtList.getStatement(0)).getExpression()).getTimeComp().getTypeName());
    }

    @Test
    void testRotationStyle() {
        StmtList statements = getStmtList("set rotation style [don't rotate v]\n");
        assertInstanceOf(SetRotationStyle.class, statements.getStatement(0));
        SetRotationStyle rotate = (SetRotationStyle) statements.getStatement(0);
        RotationStyle rotationStyle = rotate.getRotation();
        assertEquals("don't rotate", rotationStyle.getTypeName());
    }

    @Test
    void testColorEffect() {
        StmtList statements = getStmtList("set [pixelate v] effect to (2)\n");
        assertInstanceOf(SetGraphicEffectTo.class, statements.getStatement(0));
        SetGraphicEffectTo setGraphic = (SetGraphicEffectTo) statements.getStatement(0);
        GraphicEffect effect = setGraphic.getEffect();
        assertEquals("pixelate", effect.getTypeName());
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
        assertEquals("message1", ((StringLiteral) message.getMessage()).getText());
    }

    @Test
    void testAskAndWait() {
        StmtList statements = getStmtList("ask [What's your name?] and wait\n");
        assertInstanceOf(AskAndWait.class, statements.getStatement(0));
        StringExpr question = ((AskAndWait) statements.getStatement(0)).getQuestion();
        assertInstanceOf(StringExpr.class, question);
        assertEquals("What's your name?", ((StringLiteral) question).getText());
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
    void testDeeplyNestedExpression() {
        StmtList stmtList = getStmtList("wait (pick random (1) to ((60) - (((((((((((((((((((INFECTED) / (2)) / (2)) / (2)) / (2)) / (2)) / (2)) / (2)) / (2)) / (2)) / (2)) / (2)) / (2)) / (2)) / (2)) / (2)) / (2)) / (2)) / (2)))) seconds");
        WaitSeconds wait = (WaitSeconds) stmtList.getStatement(0);
        assertInstanceOf(PickRandom.class, wait.getSeconds());
    }

    @Test
    void testDeeplyNestedExpression2() {
        String stmt = """
                if <<<<<<<<<<<<<<<<touching (Soccer Ball v) ?> or <touching (Soccer Ball2 v) ?>> or <touching (Soccer Ball3 v) ?>> or <touching (Soccer Ball4 v) ?>> or <touching (Soccer Ball5 v) ?>> or <touching (Soccer Ball6 v) ?>> or <touching (Soccer Ball7 v) ?>> or <touching (Soccer Ball8 v) ?>> or <touching (Soccer Ball9 v) ?>> or <touching (Soccer Ball10 v) ?>> or <touching (Soccer Ball11 v) ?>> or <touching (Soccer Ball12 v) ?>> or <<touching (Soccer Ball14 v) ?> and <<<not <key (right arrow v) pressed?>> and <not <key (up arrow v) pressed?>>> and <<not <key (right arrow v) pressed?>> and <not <key (down arrow v) pressed?>>>>>> or <<<not <([costume # v] of (Soccer Ball15 v)?) = (2)>> and <touching (Soccer Ball15 v) ?>> or <<<not <([costume # v] of (Soccer Ball16 v)?) = (2)>> and <touching (Soccer Ball16 v) ?>> or <<not <([costume # v] of (Soccer Ball17 v)?) = (2)>> and <touching (Soccer Ball17 v) ?>>>>> or <<touching (Soccer Ball13 v) ?> and <<key (up arrow v) pressed?> or <<key (right arrow v) pressed?> or <<key (left arrow v) pressed?> or <key (down arrow v) pressed?>>>>>> or <touching (ガスターブラスター v) ?>> then
                start sound (hurt noise v)
                change [☁ 世界総ダメージ v] by (2)
                change [hp v] by (-2)
                end
                """;
        assertStatementType(stmt, IfThenStmt.class);
    }

    @Test
    void testNestedBoolExpr() {
        StmtList stmtList = getStmtList("wait until <<<(mouse x) < (-113)> and <(mouse x) > (-123)>> and <<(mouse y) < (-93)> and <(mouse y) > (-101)>>>\n");
        WaitUntil waitUntil = (WaitUntil) stmtList.getStatement(0);
        assertInstanceOf(And.class, waitUntil.getUntil());
    }

    @Test
    void testUnspecifiedBoolExpr() {
        final StmtList stmtList = getStmtList("<<> and <>>");
        final And and = assertHasExprStmt(stmtList, And.class);

        assertEquals(new And(new UnspecifiedBoolExpr(), new UnspecifiedBoolExpr(), TopNonDataBlockMetadata.emptyTopNonBlockMetadata()), and);
    }

    @Test
    void testParseScript() {
        final String stmt = "<(Lives) = (0)>";
        final StmtList stmtList = getStmtList(stmt);
        final Equals equals = assertHasExprStmt(stmtList, Equals.class);

        assertInstanceOf(Qualified.class, equals.getOperand1());
        assertInstanceOf(Variable.class, ((Qualified) equals.getOperand1()).getSecond());
    }

    @Test
    void testWaitUntilColorTouchingColor() {
        final String stmt = "wait until <color (#310000) is touching [#8000ff] ?>";
        final WaitUntil waitUntil = assertStatementType(stmt, WaitUntil.class);
        final ColorTouchingColor colourExpr = assertExpressionType(waitUntil.getUntil(), ColorTouchingColor.class);
        assertEquals(new ColorLiteral(0x31, 0x00, 0x00), colourExpr.getOperand1());
        assertEquals(new ColorLiteral(0x80, 0x00, 0xff), colourExpr.getOperand2());
    }

    @Test
    void testScriptWithCustomBlockCall() {
        final String scriptCode = """
                when green flag clicked
                Left
                """.stripIndent();
        final CallStmt callStmt = assertStatementType(scriptCode, CallStmt.class);
        assertEquals("Left", callStmt.getIdent().getName());
    }

    @Test
    void testScriptWithCustomBlockCallAndParameter() {
        final String scriptCode = """
                when green flag clicked
                Left (23)
                """.stripIndent();
        final CallStmt callStmt = assertStatementType(scriptCode, CallStmt.class);
        assertEquals("Left %s", callStmt.getIdent().getName());
        assertEquals(1, callStmt.getExpressions().getExpressions().size());
    }

    @Test
    void testScriptWithCustomBlockCallAndParameters() {
        final String scriptCode = """
                when green flag clicked
                Left (23) [abc]
                """.stripIndent();
        final CallStmt callStmt = assertStatementType(scriptCode, CallStmt.class);
        assertEquals("Left %s %s", callStmt.getIdent().getName());

        final List<Expression> arguments = callStmt.getExpressions().getExpressions();
        assertEquals(2, arguments.size());
        assertInstanceOf(NumberLiteral.class, arguments.get(0));
        assertInstanceOf(StringLiteral.class, arguments.get(1));
    }

    @Test
    void testScriptWithCustomBlockCallAndInterleavedName() {
        final String scriptCode = """
                when green flag clicked
                Left (23) of [abc]
                """.stripIndent();
        final CallStmt callStmt = assertStatementType(scriptCode, CallStmt.class);
        assertEquals("Left %s of %s", callStmt.getIdent().getName());

        final List<Expression> arguments = callStmt.getExpressions().getExpressions();
        assertEquals(2, arguments.size());
        assertInstanceOf(NumberLiteral.class, arguments.get(0));
        assertInstanceOf(StringLiteral.class, arguments.get(1));
    }

    @Test
    void testChangeColorEffect() {
        StmtList stmtList = getStmtList("change [color v] effect by (25)\nchange [fisheye v] effect by (25)\n");
        final Stmt stmt1 = stmtList.getStatement(0);
        assertInstanceOf(ChangeGraphicEffectBy.class, stmt1);
        assertEquals("color", ((ChangeGraphicEffectBy) stmt1).getEffect().getTypeName());
        final Stmt stmt2 = stmtList.getStatement(1);
        assertInstanceOf(ChangeGraphicEffectBy.class, stmt2);
        assertEquals("fisheye", ((ChangeGraphicEffectBy) stmt2).getEffect().getTypeName());
    }

    @Test
    void testSetColorEffect() {
        StmtList stmtList = getStmtList("set [color v] effect to (25)\nset [fisheye v] effect to (25)\n");
        final Stmt stmt1 = stmtList.getStatement(0);
        assertInstanceOf(SetGraphicEffectTo.class, stmt1);
        assertEquals("color", ((SetGraphicEffectTo) stmt1).getEffect().getTypeName());
        final Stmt stmt2 = stmtList.getStatement(1);
        assertInstanceOf(SetGraphicEffectTo.class, stmt2);
        assertEquals("fisheye", ((SetGraphicEffectTo) stmt2).getEffect().getTypeName());
    }

    @Test
    void testRepeat() {
        final String stmtList = """
                repeat (10)
                stop [all v]
                end
                """.stripIndent();
        final RepeatTimesStmt repeatTimesStmt = assertStatementType(stmtList, RepeatTimesStmt.class);
        assertInstanceOf(NumberLiteral.class, repeatTimesStmt.getTimes());

        assertEquals(1, repeatTimesStmt.getStmtList().getStmts().size());
        assertInstanceOf(StopAll.class, repeatTimesStmt.getStmtList().getStatement(0));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            "//abc",
            " // abc",
            "    //abc",
            "    // abc"
    })
    void testComments(final String commentSuffix) {
        final ExpressionStmt stmt = assertStatementType("((3) * (4))" + commentSuffix, ExpressionStmt.class);
        assertInstanceOf(Mult.class, stmt.getExpression());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "define abc\n",
            "define abc (sP)\n",
            "define abc (sP) def (bP)\n",
            "define abc (sP) def (bP) ghi\n",
            "define (sP)\n",
            "define <bP> abc\n"
    })
    void emptyCustomProcedureDefinition(final String definition) {
        final ScriptEntity script = getScript(definition);
        assertInstanceOf(ProcedureDefinition.class, script);
        final ProcedureDefinition procDef = (ProcedureDefinition) script;

        assertEquals(definition.replace("define ", "").trim(), procDef.getIdent().getName());
    }

    @Test
    void customProcedureDefinitionParameters() {
        final ScriptEntity script = getScript("""
                define abc (sp) bcd <bp>
                stop [all v]
                """);

        assertInstanceOf(ProcedureDefinition.class, script);
        final ProcedureDefinition procDef = (ProcedureDefinition) script;

        assertIterableEquals(
                List.of(
                        new ParameterDefinition(new StrId("sp"), new StringType(), NonDataBlockMetadata.createArtificialNonBlockMetadata(true)),
                        new ParameterDefinition(new StrId("bp"), new BooleanType(), NonDataBlockMetadata.createArtificialNonBlockMetadata(true))
                ),
                procDef.getParameterDefinitionList().getParameterDefinitions()
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "123", "ab3", "with spaces"})
    void testCustomBlockCallNoParameters(final String blockName) {
        final CallStmt stmt = assertStatementType(blockName, CallStmt.class);
        assertEquals(blockName, stmt.getIdent().getName());
        assertTrue(stmt.getExpressions().getExpressions().isEmpty());
    }

    @Test
    void testCustomBlockCallMixedNameAndParameter() {
        final String input = "custom (123) block <(1) > (3)> suffix";
        final CallStmt callStmt = assertStatementType(input, CallStmt.class);

        assertEquals("custom %s block %b suffix", callStmt.getIdent().getName());
        assertIterableEquals(
                List.of(
                        new NumberLiteral(123),
                        new BiggerThan(new NumberLiteral(1), new NumberLiteral(3), NonDataBlockMetadata.emptyNonBlockMetadata())
                ),
                callStmt.getExpressions().getExpressions()
        );
    }

    @Test
    void testEmptyBoolExpr() {
        final String scratchBlocks = """
                if <> then
                stop all sounds
                end""".stripIndent();
        final IfThenStmt stmt = assertStatementType(scratchBlocks, IfThenStmt.class);

        assertInstanceOf(UnspecifiedBoolExpr.class, stmt.getBoolExpr());
    }

    @Test
    void testMultipleCBlocksInScript() {
        final String scratchBlocks = """
                if <  > then
                end
                if <  > then
                end
                """.stripIndent();
        final ScriptEntity script = getScript(scratchBlocks);

        assertEquals(2, script.getStmtList().getStmts().size());
        assertAll(
                script.getStmtList().getStmts().stream()
                        .map(stmt -> () -> assertInstanceOf(IfThenStmt.class, stmt))
        );
    }

    @Test
    void testIfAndIfElseInScript() {
        final String scratchBlocks = """
                if <> then
                stop [this script v]
                end
                move (10) steps
                if <> then
                else
                stop [all v]
                end
                """.stripIndent();
        final ScriptEntity script = getScript(scratchBlocks);

        // in case of matching the wrong end to the first if, we would get some custom block call stmts in between
        assertEquals(3, script.getStmtList().getStmts().size());
        assertAll(
                () -> assertInstanceOf(IfThenStmt.class, script.getStmtList().getStatement(0)),
                () -> assertInstanceOf(MoveSteps.class, script.getStmtList().getStatement(1)),
                () -> assertInstanceOf(IfElseStmt.class, script.getStmtList().getStatement(2))
        );
    }

    @Test
    void testMultipleCBlocksInScriptWithStatements() {
        final String scratchBlocks = """
                if <> then
                stop all sounds
                end
                if <> then
                end
                """.stripIndent();
        final ScriptEntity script = getScript(scratchBlocks);

        assertEquals(2, script.getStmtList().getStmts().size());
        assertAll(
                script.getStmtList().getStmts().stream()
                        .map(stmt -> () -> assertInstanceOf(IfThenStmt.class, stmt))
        );
    }

    @Test
    void testForeverInsideIfElse() {
        final String scratchBlocks = """
                if <  > then
                forever
                end
                else
                stop [all v]
                end
                """.stripIndent();
        final IfElseStmt ifElse = assertStatementType(scratchBlocks, IfElseStmt.class);

        assertInstanceOf(RepeatForeverStmt.class, ifElse.getThenStmts().getStatement(0));
        assertInstanceOf(StopAll.class, ifElse.getElseStmts().getStatement(0));
    }

    @ParameterizedTest
    @ValueSource(strings = {"loudness", "timer"})
    void testBiggerThanEvent(final String choice) {
        final String scratchBlocks = String.format("when [%s v] > (10)", choice);
        final Script script = (Script) getScript(scratchBlocks);

        final Event event = script.getEvent();
        assertInstanceOf(AttributeAboveValue.class, event);

        final AttributeAboveValue aboveValue = (AttributeAboveValue) event;

        assertEquals(new EventAttribute(choice), aboveValue.getAttribute());
    }

    @Test
    void testListContainsExpr() {
        final StmtList stmtList = getStmtList("<[Test v] contains [thing] ?>");
        final ListContains containsExpr = assertHasExprStmt(stmtList, ListContains.class);

        assertEquals(new StringLiteral("thing"), containsExpr.getElement());
    }

    @Test
    void testGoToLayerStmt() {
        final String scratchBlocks = "go to [front v] layer";
        final GoToLayer goToLayer = assertStatementType(scratchBlocks, GoToLayer.class);

        assertEquals(new LayerChoice("front"), goToLayer.getLayerChoice());
    }

    @Test
    void testGoToPositionStmt() {
        final String scratchBlocks = "go to (random position v)";
        final GoToPos goToPos = assertStatementType(scratchBlocks, GoToPos.class);

        assertEquals(new RandomPos(NonDataBlockMetadata.createArtificialNonBlockMetadata(true)), goToPos.getPosition());
    }

    @Test
    void testAddToListStmt() {
        final String scratchBlocks = "add [thing] to [TestList v]";
        assertStatementType(scratchBlocks, AddTo.class);
    }


    @Test
    void testListExpression() {
        StmtList statements = getStmtList("move (list :: list) steps\n");
        assertInstanceOf(MoveSteps.class, statements.getStatement(0));

        MoveSteps move = (MoveSteps) statements.getStatement(0);
        assertInstanceOf(AsNumber.class, move.getSteps());
        AsNumber asNumber = (AsNumber) move.getSteps();
        assertInstanceOf(Qualified.class, asNumber.getOperand1());
        Qualified qualified = (Qualified) asNumber.getOperand1();
        assertInstanceOf(ScratchList.class, qualified.getSecond());
        ScratchList scratchList = (ScratchList) qualified.getSecond();
        assertEquals("list", scratchList.getName().getName());
    }

    @Test
    void testScriptList() {
        ScriptList scriptList = getScriptList("when green flag clicked\n\n(username)\n");
        assertEquals(2, scriptList.getSize());
        assertInstanceOf(GreenFlag.class, scriptList.getScript(0).getEvent());
        assertInstanceOf(Never.class, scriptList.getScript(1).getEvent());
        assertInstanceOf(ExpressionStmt.class, scriptList.getScript(1).getStmtList().getStatement(0));
    }

    @Test
    void testEmptyNum() {
        StmtList statements = getStmtList("move () steps\n");
        assertInstanceOf(MoveSteps.class, statements.getStatement(0));
        MoveSteps move = (MoveSteps) statements.getStatement(0);
        assertInstanceOf(AsNumber.class, move.getSteps());
        AsNumber asNumber = (AsNumber) move.getSteps();
        assertInstanceOf(StringLiteral.class, asNumber.getOperand1());
        assertEquals("", ((StringLiteral) asNumber.getOperand1()).getText());
    }

    @Test
    void testMathFunctionExpr() {
        StmtList statements = getStmtList("([abs v] of ())");
        final NumFunctOf numFunct = assertHasExprStmt(statements, NumFunctOf.class);
        assertEquals(NumFunct.NumFunctType.ABS, numFunct.getOperand1().getType());
    }

    @Test
    void testAttributeOfExpr() {
        StmtList statements = getStmtList("([size v] of ())");
        final AttributeOf numFunct = assertHasExprStmt(statements, AttributeOf.class);
        assertInstanceOf(AttributeFromFixed.class, numFunct.getAttribute());

        final AttributeFromFixed attr = (AttributeFromFixed) numFunct.getAttribute();
        assertEquals(FixedAttribute.FixedAttributeType.SIZE, attr.getAttribute().getType());
    }

    // region: common helper methods

    private <T extends Stmt> T assertStatementType(final String stmt, final Class<T> stmtType) {
        final StmtList stmtList = getStmtList(stmt);
        assertInstanceOf(stmtType, stmtList.getStatement(0));
        return stmtType.cast(stmtList.getStatement(0));
    }

    private <T extends Expression> T assertHasExprStmt(final StmtList stmtList, final Class<T> expressionType) {
        assertInstanceOf(ExpressionStmt.class, stmtList.getStatement(0));
        final Expression expr = ((ExpressionStmt) stmtList.getStatement(0)).getExpression();
        assertInstanceOf(expressionType, expr);

        return expressionType.cast(expr);
    }

    private <T extends Expression> T assertExpressionType(final Expression expr, Class<T> exprType) {
        assertInstanceOf(exprType, expr);
        return exprType.cast(expr);
    }

    // endregion
}
