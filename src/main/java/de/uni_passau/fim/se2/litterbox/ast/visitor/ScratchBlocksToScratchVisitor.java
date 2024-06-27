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
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.AsNumber;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AsString;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.StringExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Say;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SayForSecs;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.MoveSteps;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.TurnRight;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;

import java.util.List;

public class ScratchBlocksToScratchVisitor extends ScratchBlocksGrammarBaseVisitor<ASTNode> {

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

    // Motion Blocks

    @Override
    public MoveSteps visitMoveSteps(ScratchBlocksGrammarParser.MoveStepsContext ctx) {
        return new MoveSteps(makeNumExpr(ctx.exprOrLiteral()), new NoBlockMetadata());
    }

    @Override
    public ASTNode visitTurnRight(ScratchBlocksGrammarParser.TurnRightContext ctx) {
        return new TurnRight(makeNumExpr(ctx.exprOrLiteral()), new NoBlockMetadata());
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

    private StringExpr makeStringExpr(ScratchBlocksGrammarParser.ExprOrLiteralContext ctx){
        Expression expr = (Expression) visitExprOrLiteral(ctx);
        StringExpr stringExpr;

        if (expr instanceof StringExpr str) {
            stringExpr = str;
        } else {
            stringExpr = new AsString(expr);
        }
        return stringExpr;
    }

    @Override
    public ASTNode visitExpression(ScratchBlocksGrammarParser.ExpressionContext ctx) {
        if (ctx.stringArgument() != null) {
            return new Variable(new StrId(ctx.stringArgument().getText()));
        } else {
            return super.visitExpression(ctx);
        }
    }

    @Override
    public ASTNode visitStringArgument(ScratchBlocksGrammarParser.StringArgumentContext ctx) {
        return super.visitStringArgument(ctx);
    }
}
