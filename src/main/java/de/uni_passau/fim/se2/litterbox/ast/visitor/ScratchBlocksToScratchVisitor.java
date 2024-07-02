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
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptEntity;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Event;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.SpriteTouchingColor;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Touching;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.AsNumber;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AsString;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.StringExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.ColorLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.position.Position;
import de.uni_passau.fim.se2.litterbox.ast.model.position.RandomPos;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.ExpressionStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Say;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SayForSecs;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.GoToPos;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.MoveSteps;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.TurnRight;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;

import java.util.List;

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
        Expression expr = (Expression) visitExpression(ctx.expression());
        return new ExpressionStmt(expr);
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

    /*
    @Override
    public Expression visitExprOrLiteral(ScratchBlocksGrammarParser.ExprOrLiteralContext ctx) {
        if (ctx.numLiteral() != null) {
            return visitNumLiteral(ctx.numLiteral());
        } else if (ctx.stringLiteral() != null) {
            return (Expression) visitStringLiteral(ctx.stringLiteral());
        } else {
            return (Expression) super.visitExpression(ctx.expression());
        }
    }
     */

    @Override
    public Touching visitTouching(ScratchBlocksGrammarParser.TouchingContext ctx) {
        Touchable touchable = visitTouchingChoice(ctx.touchingChoice());
        return new Touching(touchable, new NoBlockMetadata());
    }

    @Override
    public Touchable visitTouchingChoice(ScratchBlocksGrammarParser.TouchingChoiceContext ctx) {
        if (ctx.exprOrLiteral() != null) {
            return new AsTouchable((Expression) visitExprOrLiteral(ctx.exprOrLiteral()));
        } else if (ctx.stringArgument() != null) {
            return new SpriteTouchable(new StringLiteral(ctx.stringArgument().getText()), new NoBlockMetadata());
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
        return (Position) super.visitPosition(ctx);
    }

    @Override
    public Position visitFixedPosition(ScratchBlocksGrammarParser.FixedPositionContext ctx) {
        if (ctx.getText().equals("random position")) {
            return new RandomPos(new NoBlockMetadata());
        }
        return (Position) super.visitFixedPosition(ctx);
    }

    @Override
    public SpriteTouchingColor visitTouchingColor(ScratchBlocksGrammarParser.TouchingColorContext ctx) {
        Touchable color = visitTouchingColorChoice(ctx.touchingColorChoice());
        return new SpriteTouchingColor(color, new NoBlockMetadata());
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
        return new StringLiteral(ctx.stringArgument().getText());
    }

    @Override
    public ASTNode visitExpression(ScratchBlocksGrammarParser.ExpressionContext ctx) {
        if (ctx.stringArgument() != null) {
            String stringArgument = ctx.stringArgument().getText()
                    .replaceAll("\\\\(?=[\\w" + SPECIAL_WITHOUT_BSLASH + "])", "") // Remove superfluous \
                    .replace("\\\\", "\\");     // Handle double backslash
            return new Variable(new StrId(stringArgument));
        } else if (ctx.boolExpr() != null) {
            return visitBoolExpr(ctx.boolExpr());
        } else if (ctx.numExpr() != null) {
            return visitNumExpr(ctx.numExpr());
        } else {
            return super.visitExpression(ctx);
        }
    }

    // endregion: expressions

    private NumExpr makeNumExpr(ScratchBlocksGrammarParser.ExprOrLiteralContext ctx) {
        Expression expr = (Expression) visitExprOrLiteral(ctx);
        NumExpr numExpr;

        if (expr instanceof NumExpr num) {
            numExpr = num;
        } else {
            numExpr = new AsNumber(expr);
        }
        return numExpr;
    }

    private StringExpr makeStringExpr(ScratchBlocksGrammarParser.ExprOrLiteralContext ctx) {
        Expression expr = (Expression) visitExprOrLiteral(ctx);
        StringExpr stringExpr;

        if (expr instanceof StringExpr str) {
            stringExpr = str;
        } else {
            stringExpr = new AsString(expr);
        }
        return stringExpr;
    }
}
