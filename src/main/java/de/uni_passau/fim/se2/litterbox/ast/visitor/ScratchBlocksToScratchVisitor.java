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

import de.uni_passau.fim.se2.litterbox.ScratchBlocksGrammarBaseVisitor;
import de.uni_passau.fim.se2.litterbox.ScratchBlocksGrammarParser;
import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.ElementChoice;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.WithExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Event;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.ComparableExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.Attribute;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.AttributeFromFixed;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.AttributeFromVariable;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.FixedAttribute;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.ColorLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.position.FromExpression;
import de.uni_passau.fim.se2.litterbox.ast.model.position.MousePos;
import de.uni_passau.fim.se2.litterbox.ast.model.position.Position;
import de.uni_passau.fim.se2.litterbox.ast.model.position.RandomPos;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.ExpressionStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Say;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SayForSecs;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.GoToPos;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.MoveSteps;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.TurnRight;
import de.uni_passau.fim.se2.litterbox.ast.model.timecomp.TimeComp;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.*;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.color.Color;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;

import java.util.List;

import static de.uni_passau.fim.se2.litterbox.ast.parser.KeyParser.*;

public class ScratchBlocksToScratchVisitor extends ScratchBlocksGrammarBaseVisitor<ASTNode> {

    private static final String SPECIAL_WITHOUT_BSLASH = "[!\"#$%&'()*+,\\-./:;<=>?@\\[\\]^_`{|}~]";

    @Override
    public ScriptEntity visitScript(ScratchBlocksGrammarParser.ScriptContext ctx) {
        if (ctx.expressionStmt() != null) {
            return new Script(new Never(), new StmtList(visitExpressionStmt(ctx.expressionStmt())));
        } else if (ctx.stmtList() != null) {
            if (ctx.event() != null) {
                return new Script((Event) visitEvent(ctx.event()), visitStmtList(ctx.stmtList()));
            } else {
                return new Script(new Never(), visitStmtList(ctx.stmtList()));
            }
        } else if (ctx.event() != null) {
            return new Script((Event) visitEvent(ctx.event()), new StmtList());
        } else if (ctx.customBlock() != null) {
            return (ScriptEntity) visit(ctx.customBlock());
        } else {
            return (ScriptEntity) super.visitScript(ctx);
        }
    }

    @Override
    public StmtList visitStmtList(ScratchBlocksGrammarParser.StmtListContext ctx) {
        final List<Stmt> stmts = ctx.stmt().stream().map(this::visitStmt).toList();
        return new StmtList(stmts);
    }

    // region: statements

    @Override
    public Stmt visitStmt(ScratchBlocksGrammarParser.StmtContext ctx) {
        if (ctx.controlStmt() != null) {
            return (Stmt) visitControlStmt(ctx.controlStmt());
            // todo: other cases
        } else {
            return (Stmt) super.visitStmt(ctx);
        }
    }

    @Override
    public Stmt visitExpressionStmt(ScratchBlocksGrammarParser.ExpressionStmtContext ctx) {
        return new ExpressionStmt(visitExpression(ctx.expression()));
    }

    @Override
    public MoveSteps visitMoveSteps(ScratchBlocksGrammarParser.MoveStepsContext ctx) {
        return new MoveSteps(makeNumExpr(ctx.exprOrLiteral()), new NoBlockMetadata());
    }

    @Override
    public ASTNode visitTurnRight(ScratchBlocksGrammarParser.TurnRightContext ctx) {
        return new TurnRight(makeNumExpr(ctx.exprOrLiteral()), new NoBlockMetadata());
    }

    @Override
    public ASTNode visitGoToPos(ScratchBlocksGrammarParser.GoToPosContext ctx) {
        Position position = visitPosition(ctx.position());
        return new GoToPos(position, new NoBlockMetadata());
    }

    @Override
    public Say visitSay(ScratchBlocksGrammarParser.SayContext ctx) {
        return new Say(makeStringExpr(ctx.exprOrLiteral()), new NoBlockMetadata());
    }

    @Override
    public SayForSecs visitSaySeconds(ScratchBlocksGrammarParser.SaySecondsContext ctx) {
        return new SayForSecs(makeStringExpr(ctx.text), makeNumExpr(ctx.time), new NoBlockMetadata());
    }

    // endregion: statements

    // region: expressions

    // subregion: bool expressions
    @Override
    public Expression visitExprOrLiteral(ScratchBlocksGrammarParser.ExprOrLiteralContext ctx) {
        if (ctx.numLiteral() != null) {
            return visitNumLiteral(ctx.numLiteral());
        } else if (ctx.stringLiteral() != null) {
            return visitStringLiteral(ctx.stringLiteral());
        } else {
            return visitExpression(ctx.expression());
        }
    }

    @Override
    public Touching visitTouching(ScratchBlocksGrammarParser.TouchingContext ctx) {
        Touchable touchable = visitTouchingChoice(ctx.touchingChoice());
        return new Touching(touchable, new NoBlockMetadata());
    }

    @Override
    public Touchable visitTouchingChoice(ScratchBlocksGrammarParser.TouchingChoiceContext ctx) {
        if (ctx.exprOrLiteral() != null) {
            return new AsTouchable(visitExprOrLiteral(ctx.exprOrLiteral()));
        } else if (ctx.stringArgument() != null) {
            return new SpriteTouchable(visitStringArgument(ctx.stringArgument()), new NoBlockMetadata());
        } else if (ctx.fixedTouching() != null) {
            return visitFixedTouching(ctx.fixedTouching());
        }
        return (Touchable) super.visitTouchingChoice(ctx);
    }

    @Override
    public Touchable visitFixedTouching(ScratchBlocksGrammarParser.FixedTouchingContext ctx) {
        if (ctx.getText().equals("mouse-pointer")) {
            return new MousePointer(new NoBlockMetadata());
        } else {
            return new Edge(new NoBlockMetadata());
        }
    }

    @Override
    public Position visitPosition(ScratchBlocksGrammarParser.PositionContext ctx) {
        if (ctx.fixedPosition() != null) {
            return visitFixedPosition(ctx.fixedPosition());
        }
        return new FromExpression(makeStringExpr(ctx.exprOrLiteral()), new NoBlockMetadata());
    }

    @Override
    public Position visitFixedPosition(ScratchBlocksGrammarParser.FixedPositionContext ctx) {
        if (ctx.getText().equals("random position")) {
            return new RandomPos(new NoBlockMetadata());
        } else if (ctx.mousePointer() != null) {
            return new MousePos(new NoBlockMetadata());
        } else {
            return new FromExpression(visitStringArgument(ctx.stringArgument()), new NoBlockMetadata());
        }
    }

    @Override
    public SpriteTouchingColor visitTouchingColor(ScratchBlocksGrammarParser.TouchingColorContext ctx) {
        Touchable color = visitTouchingColorChoice(ctx.touchingColorChoice());
        return new SpriteTouchingColor(color, new NoBlockMetadata());
    }

    @Override
    public ColorTouchingColor visitColorTouchingColor(ScratchBlocksGrammarParser.ColorTouchingColorContext ctx) {
        return new ColorTouchingColor((Color) visitTouchingColorChoice(ctx.firstColor), (Color) visitTouchingColorChoice(ctx.secondColor), new NoBlockMetadata());
    }

    @Override
    public Touchable visitTouchingColorChoice(ScratchBlocksGrammarParser.TouchingColorChoiceContext ctx) {
        if (ctx.exprOrLiteral() != null) {
            return new AsTouchable((Expression) visitExprOrLiteral(ctx.exprOrLiteral()));
        } else {
            String rgbCode = ctx.HEX().getText();

            long rNumber = Long.parseLong(rgbCode.substring(1, 3), 16);
            long gNumber = Long.parseLong(rgbCode.substring(3, 5), 16);
            long bNumber = Long.parseLong(rgbCode.substring(5, 7), 16);

            return new ColorLiteral(rNumber, gNumber, bNumber);
        }
    }

    @Override
    public IsKeyPressed visitKeyPressed(ScratchBlocksGrammarParser.KeyPressedContext ctx) {
        return new IsKeyPressed(visitKeySelect(ctx.keySelect()), new NoBlockMetadata());
    }

    @Override
    public Key visitKeySelect(ScratchBlocksGrammarParser.KeySelectContext ctx) {
        if (ctx.key() != null) {
            return visitKey(ctx.key());
        } else {
            return new Key(makeNumExpr(ctx.exprOrLiteral()), new NoBlockMetadata());
        }
    }

    @Override
    public Key visitKey(ScratchBlocksGrammarParser.KeyContext ctx) {
        return switch (ctx.getText()) {
            case "space" -> new Key(new NumberLiteral(SPACE), new NoBlockMetadata());
            case "up arrow" -> new Key(new NumberLiteral(UPARROW), new NoBlockMetadata());
            case "down arrow" -> new Key(new NumberLiteral(DOWNARROW), new NoBlockMetadata());
            case "left arrow" -> new Key(new NumberLiteral(LEFTARROW), new NoBlockMetadata());
            case "right arrow" -> new Key(new NumberLiteral(RIGHTARROW), new NoBlockMetadata());
            case "any" -> new Key(new NumberLiteral(ANYKEY), new NoBlockMetadata());
            default -> new Key(new NumberLiteral(ctx.getText().charAt(0)), new NoBlockMetadata());
        };
    }

    @Override
    public IsMouseDown visitMouseDown(ScratchBlocksGrammarParser.MouseDownContext ctx) {
        return new IsMouseDown(new NoBlockMetadata());
    }

    @Override
    public BiggerThan visitGreaterThan(ScratchBlocksGrammarParser.GreaterThanContext ctx) {
        return new BiggerThan((ComparableExpr) visitExprOrLiteral(ctx.firstExpr), (ComparableExpr) visit(ctx.secondExpr), new NoBlockMetadata());
    }

    @Override
    public LessThan visitLessThan(ScratchBlocksGrammarParser.LessThanContext ctx) {
        return new LessThan((ComparableExpr) visitExprOrLiteral(ctx.firstExpr), (ComparableExpr) visit(ctx.secondExpr), new NoBlockMetadata());
    }

    @Override
    public Equals visitEqual(ScratchBlocksGrammarParser.EqualContext ctx) {
        return new Equals((ComparableExpr) visitExprOrLiteral(ctx.firstExpr), (ComparableExpr) visit(ctx.secondExpr), new NoBlockMetadata());
    }

    @Override
    public Not visitNot(ScratchBlocksGrammarParser.NotContext ctx) {
        return new Not(makeBoolExpr(ctx.exprOrLiteral()), new NoBlockMetadata());
    }

    @Override
    public And visitAnd(ScratchBlocksGrammarParser.AndContext ctx) {
        return new And(makeBoolExpr(ctx.firstExpr), makeBoolExpr(ctx.secondExpr), new NoBlockMetadata());
    }

    @Override
    public Or visitOr(ScratchBlocksGrammarParser.OrContext ctx) {
        return new Or(makeBoolExpr(ctx.firstExpr), makeBoolExpr(ctx.secondExpr), new NoBlockMetadata());
    }

    @Override
    public StringContains visitContains(ScratchBlocksGrammarParser.ContainsContext ctx) {
        return new StringContains(makeStringExpr(ctx.firstExpr), makeStringExpr(ctx.secondExpr), new NoBlockMetadata());
    }

    @Override
    public ListContains visitListContains(ScratchBlocksGrammarParser.ListContainsContext ctx) {
        return new ListContains(new StrId(visitStringArgument(ctx.stringArgument())), visitExprOrLiteral(ctx.exprOrLiteral()), new NoBlockMetadata());
    }

    //end subregion: bool expressions

    //subregion num expression

    @Override
    public PositionX visitXPosition(ScratchBlocksGrammarParser.XPositionContext ctx) {
        return new PositionX(new NoBlockMetadata());
    }

    @Override
    public PositionY visitYPosition(ScratchBlocksGrammarParser.YPositionContext ctx) {
        return new PositionY(new NoBlockMetadata());
    }

    @Override
    public Direction visitDirection(ScratchBlocksGrammarParser.DirectionContext ctx) {
        return new Direction(new NoBlockMetadata());
    }

    @Override
    public Costume visitNumCostume(ScratchBlocksGrammarParser.NumCostumeContext ctx) {
        return new Costume(visitNameNum(ctx.nameNum()), new NoBlockMetadata());
    }

    @Override
    public NameNum visitNameNum(ScratchBlocksGrammarParser.NameNumContext ctx) {
        return new NameNum(ctx.getText());
    }

    @Override
    public Backdrop visitNumBackdrop(ScratchBlocksGrammarParser.NumBackdropContext ctx) {
        return new Backdrop(visitNameNum(ctx.nameNum()), new NoBlockMetadata());
    }

    @Override
    public Size visitSize(ScratchBlocksGrammarParser.SizeContext ctx) {
        return new Size(new NoBlockMetadata());
    }

    @Override
    public Volume visitVolume(ScratchBlocksGrammarParser.VolumeContext ctx) {
        return new Volume(new NoBlockMetadata());
    }

    @Override
    public DistanceTo visitDistanceTo(ScratchBlocksGrammarParser.DistanceToContext ctx) {
        return new DistanceTo(visitDistanceChoice(ctx.distanceChoice()), new NoBlockMetadata());
    }

    @Override
    public Position visitDistanceChoice(ScratchBlocksGrammarParser.DistanceChoiceContext ctx) {
        if (ctx.mousePointer() != null) {
            return new MousePos(new NoBlockMetadata());
        } else if (ctx.stringArgument() != null) {
            return new FromExpression(visitStringArgument(ctx.stringArgument()), new NoBlockMetadata());
        } else {
            return new FromExpression(makeStringExpr(ctx.exprOrLiteral()), new NoBlockMetadata());
        }
    }

    @Override
    public Answer visitAnswer(ScratchBlocksGrammarParser.AnswerContext ctx) {
        return new Answer(new NoBlockMetadata());
    }

    @Override
    public MouseX visitMouseX(ScratchBlocksGrammarParser.MouseXContext ctx) {
        return new MouseX(new NoBlockMetadata());
    }

    @Override
    public MouseY visitMouseY(ScratchBlocksGrammarParser.MouseYContext ctx) {
        return new MouseY(new NoBlockMetadata());
    }

    @Override
    public Loudness visitLoudness(ScratchBlocksGrammarParser.LoudnessContext ctx) {
        return new Loudness(new NoBlockMetadata());
    }

    @Override
    public Timer visitTimer(ScratchBlocksGrammarParser.TimerContext ctx) {
        return new Timer(new NoBlockMetadata());
    }

    @Override
    public AttributeOf visitActorAttribute(ScratchBlocksGrammarParser.ActorAttributeContext ctx) {
        return new AttributeOf(visitAttributeChoice(ctx.attributeChoice()), visitElement(ctx.element()), new NoBlockMetadata());
    }

    @Override
    public ElementChoice visitElement(ScratchBlocksGrammarParser.ElementContext ctx) {
        if (ctx.stringArgument() != null) {
            return new WithExpr(visitStringArgument(ctx.stringArgument()), new NoBlockMetadata());
        } else {
            return new WithExpr(visitExprOrLiteral(ctx.exprOrLiteral()), new NoBlockMetadata());
        }
    }

    @Override
    public Attribute visitAttributeChoice(ScratchBlocksGrammarParser.AttributeChoiceContext ctx) {
        if (ctx.fixedAttribute() != null) {
            return new AttributeFromFixed(visitFixedAttribute(ctx.fixedAttribute()));
        } else {
            return new AttributeFromVariable(new Variable(new StrId(visitStringArgument(ctx.stringArgument()))));
        }
    }

    @Override
    public FixedAttribute visitFixedAttribute(ScratchBlocksGrammarParser.FixedAttributeContext ctx) {
        return new FixedAttribute(ctx.getText());
    }

    @Override
    public Current visitCurrentTime(ScratchBlocksGrammarParser.CurrentTimeContext ctx) {
        return new Current(visitCurrentChoice(ctx.currentChoice()), new NoBlockMetadata());
    }

    @Override
    public TimeComp visitCurrentChoice(ScratchBlocksGrammarParser.CurrentChoiceContext ctx) {
        if (ctx.getText().equals("day of the week")) {
            return new TimeComp("dayofweek");
        }
        return new TimeComp(ctx.getText());
    }

    @Override
    public DaysSince2000 visitDaysSince(ScratchBlocksGrammarParser.DaysSinceContext ctx) {
        return new DaysSince2000(new NoBlockMetadata());
    }

    @Override
    public Username visitUserName(ScratchBlocksGrammarParser.UserNameContext ctx) {
        return new Username(new NoBlockMetadata());
    }

    @Override
    public Add visitAddition(ScratchBlocksGrammarParser.AdditionContext ctx) {
        return new Add(makeNumExpr(ctx.firstExpr), makeNumExpr(ctx.secondExpr), new NoBlockMetadata());
    }

    @Override
    public Minus visitSubtraction(ScratchBlocksGrammarParser.SubtractionContext ctx) {
        return new Minus(makeNumExpr(ctx.firstExpr), makeNumExpr(ctx.secondExpr), new NoBlockMetadata());
    }

    @Override
    public Mult visitMultiplication(ScratchBlocksGrammarParser.MultiplicationContext ctx) {
        return new Mult(makeNumExpr(ctx.firstExpr), makeNumExpr(ctx.secondExpr), new NoBlockMetadata());
    }

    @Override
    public Div visitDivision(ScratchBlocksGrammarParser.DivisionContext ctx) {
        return new Div(makeNumExpr(ctx.firstExpr), makeNumExpr(ctx.secondExpr), new NoBlockMetadata());
    }

    @Override
    public PickRandom visitPickRandom(ScratchBlocksGrammarParser.PickRandomContext ctx) {
        return new PickRandom(makeNumExpr(ctx.firstExpr), makeNumExpr(ctx.secondExpr), new NoBlockMetadata());
    }

    @Override
    public Join visitJoin(ScratchBlocksGrammarParser.JoinContext ctx) {
        return new Join(makeStringExpr(ctx.firstExpr), makeStringExpr(ctx.secondExpr), new NoBlockMetadata());
    }

    @Override
    public LetterOf visitGetLetterAtIndex(ScratchBlocksGrammarParser.GetLetterAtIndexContext ctx) {
        return new LetterOf(makeNumExpr(ctx.firstExpr), makeStringExpr(ctx.secondExpr), new NoBlockMetadata());
    }

    @Override
    public LengthOfString visitLengthOf(ScratchBlocksGrammarParser.LengthOfContext ctx) {
        return new LengthOfString(makeStringExpr(ctx.exprOrLiteral()), new NoBlockMetadata());
    }

    @Override
    public Mod visitModulo(ScratchBlocksGrammarParser.ModuloContext ctx) {
        return new Mod(makeNumExpr(ctx.firstExpr), makeNumExpr(ctx.secondExpr), new NoBlockMetadata());
    }

    @Override
    public Round visitRound(ScratchBlocksGrammarParser.RoundContext ctx) {
        return new Round(makeNumExpr(ctx.exprOrLiteral()), new NoBlockMetadata());
    }

    @Override
    public NumFunctOf visitMathFunction(ScratchBlocksGrammarParser.MathFunctionContext ctx) {
        return new NumFunctOf(visitMathChoice(ctx.mathChoice()), makeNumExpr(ctx.exprOrLiteral()), new NoBlockMetadata());
    }

    @Override
    public NumFunct visitMathChoice(ScratchBlocksGrammarParser.MathChoiceContext ctx) {
        return new NumFunct(ctx.getText());
    }

    @Override
    public ItemOfVariable visitItemAtIndex(ScratchBlocksGrammarParser.ItemAtIndexContext ctx) {
        return new ItemOfVariable(makeNumExpr(ctx.exprOrLiteral()), new StrId(visitStringArgument(ctx.stringArgument())), new NoBlockMetadata());
    }

    @Override
    public IndexOf visitIndexOfItem(ScratchBlocksGrammarParser.IndexOfItemContext ctx) {
        return new IndexOf(visitExprOrLiteral(ctx.exprOrLiteral()), new StrId(visitStringArgument(ctx.stringArgument())), new NoBlockMetadata());
    }

    @Override
    public LengthOfVar visitLengthOfList(ScratchBlocksGrammarParser.LengthOfListContext ctx) {
        return new LengthOfVar(new StrId(visitStringArgument(ctx.stringArgument())), new NoBlockMetadata());
    }

    //end subregion: num expressions

    @Override
    public NumberLiteral visitNumLiteral(ScratchBlocksGrammarParser.NumLiteralContext ctx) {
        final String value;
        if (ctx.DIGIT() != null) {
            value = ctx.DIGIT().getText();
        } else {
            value = ctx.NUMBER().getText();
        }

        return new NumberLiteral(Double.parseDouble(value));
    }

    @Override
    public StringLiteral visitStringLiteral(ScratchBlocksGrammarParser.StringLiteralContext ctx) {
        return visitStringArgument(ctx.stringArgument());
    }

    @Override
    public Expression visitExpression(ScratchBlocksGrammarParser.ExpressionContext ctx) {
        if (ctx.stringArgument() != null) {
            return new Variable(new StrId(visitStringArgument(ctx.stringArgument())));
        } else if (ctx.boolExpr() != null) {
            return (Expression) visitBoolExpr(ctx.boolExpr());
        } else if (ctx.numExpr() != null) {
            return (Expression) visitNumExpr(ctx.numExpr());
        } else {
            return (Expression) super.visitExpression(ctx);
        }
    }

    public StringLiteral visitStringArgument(ScratchBlocksGrammarParser.StringArgumentContext ctx) {
        return new StringLiteral(ctx.getText()
                .replaceAll("\\\\(?=[\\w" + SPECIAL_WITHOUT_BSLASH + "])", "") // Remove superfluous \
                .replace("\\\\", "\\")); // Handle double backslash
    }

    // endregion: expressions

    private NumExpr makeNumExpr(ScratchBlocksGrammarParser.ExprOrLiteralContext ctx) {
        Expression expr = visitExprOrLiteral(ctx);
        NumExpr numExpr;

        if (expr instanceof NumExpr num) {
            numExpr = num;
        } else {
            numExpr = new AsNumber(expr);
        }
        return numExpr;
    }

    private StringExpr makeStringExpr(ScratchBlocksGrammarParser.ExprOrLiteralContext ctx) {
        Expression expr = visitExprOrLiteral(ctx);
        StringExpr stringExpr;

        if (expr instanceof StringExpr str) {
            stringExpr = str;
        } else {
            stringExpr = new AsString(expr);
        }
        return stringExpr;
    }

    private BoolExpr makeBoolExpr(ScratchBlocksGrammarParser.ExprOrLiteralContext ctx) {
        Expression expr = visitExprOrLiteral(ctx);
        BoolExpr boolExpr;

        if (expr instanceof BoolExpr bool) {
            boolExpr = bool;
        } else {
            boolExpr = new AsBool(expr);
        }
        return boolExpr;
    }
}
