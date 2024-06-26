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
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.MoveSteps;

public class ScratchBlocksToScratchVisitor extends ScratchBlocksGrammarBaseVisitor<ASTNode> {

    @Override
    public ASTNode visitMoveSteps(ScratchBlocksGrammarParser.MoveStepsContext ctx) {

        Expression expr = (Expression) this.visitExprOrLiteral(ctx.exprOrLiteral());
        NumExpr numExpr;
        if (!(expr instanceof NumExpr)) {
            numExpr = new AsNumber(expr);
        } else {
            numExpr = (NumExpr) expr;
        }
        return new MoveSteps(numExpr, new NoBlockMetadata());
    }

    @Override
    public ASTNode visitExprOrLiteral(ScratchBlocksGrammarParser.ExprOrLiteralContext ctx) {
        return super.visitExprOrLiteral(ctx);
    }

    @Override
    public ASTNode visitNumLiteral(ScratchBlocksGrammarParser.NumLiteralContext ctx) {
        return new NumberLiteral(Double.parseDouble(ctx.NUMBER().getText()));
    }

    @Override
    protected ASTNode aggregateResult(ASTNode aggregate, ASTNode nextResult) {
        if (aggregate instanceof Stmt && nextResult instanceof Stmt) {
            StmtList list = new StmtList((Stmt) aggregate, (Stmt) nextResult);
            return list;
        } else if (nextResult == null) {
            return aggregate;
        } else if (aggregate instanceof StmtList && nextResult instanceof Stmt) {
            ((StmtList) aggregate).getStmts().add((Stmt) nextResult);
            return aggregate;
        } else if (aggregate instanceof Stmt && nextResult instanceof StmtList) {
            ((StmtList) nextResult).getStmts().add((Stmt) aggregate);
            return nextResult;
        }
        return nextResult;
    }
}
