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
package de.uni_passau.fim.se2.litterbox.ast.new_parser;

import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.UnspecifiedExpression;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.ExpressionStmt;
import de.uni_passau.fim.se2.litterbox.ast.new_parser.raw_ast.*;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParserState;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.SymbolTable;

abstract class ExprConverter {

    protected ExprConverter() {
    }

    static Expression convertExpr(
            final ProgramParserState state,
            final RawBlock.RawRegularBlock containingBlock,
            final RawInput exprBlock
    ) {
        if (NumExprConverter.parseableAsNumExpr(state.getCurrentTarget(), exprBlock)) {
            return NumExprConverter.convertNumExpr(state, containingBlock, exprBlock);
        } else if (StringExprConverter.parseableAsStringExpr(state.getCurrentTarget(), exprBlock)) {
            return StringExprConverter.convertStringExpr(state, containingBlock, exprBlock);
        } else if (BoolExprConverter.parseableAsBoolExpr(state.getCurrentTarget(), exprBlock)) {
            return BoolExprConverter.convertBoolExpr(state, containingBlock, exprBlock);
        } else if (DataExprConverter.parseableAsDataExpr(state.getCurrentTarget(), exprBlock)) {
            return DataExprConverter.convertDataExpr(state, containingBlock, exprBlock);
        } else {
            return new UnspecifiedExpression();
        }
    }

    static ExpressionStmt convertExprStmt(
            final ProgramParserState state,
            final RawBlockId blockId,
            final RawBlock exprBlock
    ) {
        throw new UnsupportedOperationException("todo: expr statement");
    }

    static ExpressionStmt parseExprBlock(
            final ProgramParserState state,
            final RawBlock exprBlock
    ) {
        final SymbolTable symbolTable = state.getSymbolTable();

        if (exprBlock instanceof RawBlock.RawRegularBlock regularExprBlock) {
            throw new UnsupportedOperationException("todo: expression statements");
        } else if (exprBlock instanceof RawBlock.RawVariable variable) {
            throw new UnsupportedOperationException("todo: variable expression statement");
        } else if (exprBlock instanceof RawBlock.RawList list) {
            throw new UnsupportedOperationException("todo: list expression statement");
        } else {
            throw new InternalParsingException("Unknown format for expression statement.");
        }
    }

    protected static boolean hasCorrectShadow(final RawInput exprBlock) {
        return exprBlock.shadowType() == ShadowType.SHADOW || (
                exprBlock.shadowType() == ShadowType.NO_SHADOW && !(exprBlock.input() instanceof BlockRef.IdRef)
        );
    }
}
