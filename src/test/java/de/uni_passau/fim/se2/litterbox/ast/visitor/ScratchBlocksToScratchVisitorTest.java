/*
 * Copyright (C) 2019-2022 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.ast.visitor;

import de.uni_passau.fim.se2.litterbox.ScratchBlocksGrammarLexer;
import de.uni_passau.fim.se2.litterbox.ScratchBlocksGrammarParser;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BiggerThan;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.ColorTouchingColor;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.IsKeyPressed;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.SpriteTouchingColor;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.AsNumber;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.Current;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AttributeOf;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.Costume;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.StringExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.AttributeFromFixed;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.AttributeFromVariable;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.FixedAttribute;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.position.RandomPos;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.ExpressionStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.GraphicEffect;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.SetGraphicEffectTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Say;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.GoToPos;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.MoveSteps;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.RotationStyle;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.SetRotationStyle;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopAll;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ScratchBlocksToScratchVisitorTest {

    private Script getScript(String scratchBlocksInput) {
        ScratchBlocksGrammarLexer lexer = new ScratchBlocksGrammarLexer(CharStreams.fromString(scratchBlocksInput));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ScratchBlocksGrammarParser parser = new ScratchBlocksGrammarParser(tokens);
        ParseTree tree = parser.actor();

        ScratchBlocksToScratchVisitor vis = new ScratchBlocksToScratchVisitor();
        ASTNode node = vis.visit(tree);
        return ((Script) node);
    }

    private StmtList getStmtList(String scratchBlocksInput) {
        return getScript(scratchBlocksInput).getStmtList();
    }

    @Test
    public void testSayWithLiteral() {
        StmtList statement = getStmtList("say [ja!]\n");
        Assertions.assertInstanceOf(Say.class, statement.getStatement(0));
        StringExpr expr = ((Say) statement.getStatement(0)).getString();
        Assertions.assertInstanceOf(StringLiteral.class, expr);
        Assertions.assertEquals("ja!", ((StringLiteral) expr).getText());
    }

    @Test
    public void testExprOrLiteralNum() {
        StmtList statements = getStmtList("move (10) steps\n");
        Assertions.assertInstanceOf(MoveSteps.class, statements.getStatement(0));
        MoveSteps moveSteps = (MoveSteps) statements.getStatement(0);
        double value = ((NumberLiteral) moveSteps.getSteps()).getValue();
        Assertions.assertEquals(10, value);
    }

    @Test
    public void testExprOrLiteralString() {
        StmtList statements = getStmtList("move [a] steps\n");
        Assertions.assertInstanceOf(MoveSteps.class, statements.getStatement(0));
        MoveSteps moveSteps = (MoveSteps) statements.getStatement(0);
        String literal = ((StringLiteral) ((AsNumber) moveSteps.getSteps()).getOperand1()).getText();
        Assertions.assertEquals("a", literal);
    }

    @Test
    public void testExprOrLiteralExpression() {
        StmtList statements = getStmtList("move (\\)\\(\\_\\$\\\\\\\\\\\\a) steps\n");
        Assertions.assertInstanceOf(MoveSteps.class, statements.getStatement(0));
        MoveSteps moveSteps = (MoveSteps) statements.getStatement(0);
        String variableName = ((Variable) ((AsNumber) moveSteps.getSteps()).getOperand1()).getName().getName();
        Assertions.assertEquals(")(_$\\\\\\a", variableName);
    }

    @Test
    public void testPositionFixedRandom() {
        StmtList statements = getStmtList("go to (random position v)\n");
        Assertions.assertInstanceOf(GoToPos.class, statements.getStatement(0));
        GoToPos goToPos = (GoToPos) statements.getStatement(0);
        Assertions.assertInstanceOf(RandomPos.class, goToPos.getPosition());
    }

    @Test
    public void testTouchingColor() {
        StmtList stmtList = getStmtList("<touching color (#ff00ff)?>\n");
        Assertions.assertInstanceOf(ExpressionStmt.class, stmtList.getStatement(0));
        Assertions.assertInstanceOf(SpriteTouchingColor.class, ((ExpressionStmt) stmtList.getStatement(0)).getExpression());
    }

    @Test
    public void testColorTouchingColor() {
        StmtList stmtList = getStmtList("<color (#ff00ff) is touching (#ffff00)?>\n");
        Assertions.assertInstanceOf(ExpressionStmt.class, stmtList.getStatement(0));
        Assertions.assertInstanceOf(ColorTouchingColor.class, ((ExpressionStmt) stmtList.getStatement(0)).getExpression());
    }

    @Test
    public void testKeyPressed() {
        StmtList stmtList = getStmtList("<key (a v) pressed?>\n");
        Assertions.assertInstanceOf(ExpressionStmt.class, stmtList.getStatement(0));
        Assertions.assertInstanceOf(IsKeyPressed.class, ((ExpressionStmt) stmtList.getStatement(0)).getExpression());
    }

    @Test
    public void testBiggerThan() {
        StmtList stmtList = getStmtList("<(7) > (2)>\n");
        Assertions.assertInstanceOf(ExpressionStmt.class, stmtList.getStatement(0));
        Assertions.assertInstanceOf(BiggerThan.class, ((ExpressionStmt) stmtList.getStatement(0)).getExpression());
    }

    @Test
    public void testCostumeName() {
        StmtList stmtList = getStmtList("(costume [name v])\n");
        Assertions.assertInstanceOf(ExpressionStmt.class, stmtList.getStatement(0));
        Assertions.assertInstanceOf(Costume.class, ((ExpressionStmt) stmtList.getStatement(0)).getExpression());
    }

    @Test
    public void testAttributeOf() {
        StmtList stmtList = getStmtList("([costume # v] of (Sprite1 v))\n");
        Assertions.assertInstanceOf(ExpressionStmt.class, stmtList.getStatement(0));
        Assertions.assertInstanceOf(AttributeOf.class, ((ExpressionStmt) stmtList.getStatement(0)).getExpression());
        Assertions.assertInstanceOf(AttributeFromFixed.class, ((AttributeOf) ((ExpressionStmt) stmtList.getStatement(0)).getExpression()).getAttribute());
        stmtList = getStmtList("([var v] of (Sprite1 v))\n");
        Assertions.assertInstanceOf(ExpressionStmt.class, stmtList.getStatement(0));
        Assertions.assertInstanceOf(AttributeOf.class, ((ExpressionStmt) stmtList.getStatement(0)).getExpression());
        Assertions.assertInstanceOf(AttributeFromVariable.class, ((AttributeOf) ((ExpressionStmt) stmtList.getStatement(0)).getExpression()).getAttribute());
    }

    @Test
    public void testCurrent() {
        StmtList stmtList = getStmtList("(current [day of the week v])\n");
        Assertions.assertInstanceOf(ExpressionStmt.class, stmtList.getStatement(0));
        Assertions.assertInstanceOf(Current.class, ((ExpressionStmt) stmtList.getStatement(0)).getExpression());
        Assertions.assertEquals("dayofweek", ((Current) ((ExpressionStmt) stmtList.getStatement(0)).getExpression()).getTimeComp().getTypeName());
    }

    @Test
    public void testRotationStyle() {
        StmtList statements = getStmtList("set rotation style [don't rotate v]\n");
        Assertions.assertInstanceOf(SetRotationStyle.class, statements.getStatement(0));
        SetRotationStyle rotate = (SetRotationStyle) statements.getStatement(0);
        RotationStyle rotationStyle = rotate.getRotation();
        Assertions.assertEquals("don't rotate", rotationStyle.getTypeName());
    }

    @Test
    public void testColorEffect() {
        StmtList statements = getStmtList("set [pixelate v] effect to (2)\n");
        Assertions.assertInstanceOf(SetGraphicEffectTo.class, statements.getStatement(0));
        SetGraphicEffectTo setGraphic = (SetGraphicEffectTo) statements.getStatement(0);
        GraphicEffect effect = setGraphic.getEffect();
        Assertions.assertEquals("pixelate", effect.getTypeName());
    }

    @Test
    public void testStopAll() {
        StmtList statements = getStmtList("stop [all v]\n");
        Assertions.assertInstanceOf(StopAll.class, statements.getStatement(0));
    }

    @Test
    public void testForever() {
        StmtList statements = getStmtList("forever\n stop [all v]\n end\n");
        Assertions.assertInstanceOf(RepeatForeverStmt.class, statements.getStatement(0));
        RepeatForeverStmt forever = (RepeatForeverStmt) statements.getStatement(0);
        Assertions.assertInstanceOf(StopAll.class,forever.getStmtList().getStatement(0));
    }
}
