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

import java.util.List;

public class ScratchBlocksToScratchVisitor extends ScratchBlocksGrammarBaseVisitor<ASTNode> {

    @Override
    public StmtList visitStmtList(ScratchBlocksGrammarParser.StmtListContext ctx) {
        final List<Stmt> stmts = ctx.stmt().stream().map(stmt -> (Stmt) stmt.accept(this)).toList();
        return new StmtList(stmts);
    }

    // region: statements

    @Override
    public MoveSteps visitMoveSteps(ScratchBlocksGrammarParser.MoveStepsContext ctx) {
        Expression expr = (Expression) ctx.exprOrLiteral().accept(this);
        NumExpr numExpr;

        if (expr instanceof NumExpr num) {
            numExpr = num;
        } else {
            numExpr = new AsNumber(expr);
        }

        return new MoveSteps(numExpr, new NoBlockMetadata());
    }

    // endregion: statements

    // region: expressions

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

    // endregion: expressions
}
