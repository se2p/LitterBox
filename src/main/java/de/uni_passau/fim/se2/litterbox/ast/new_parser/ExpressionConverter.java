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
import de.uni_passau.fim.se2.litterbox.ast.new_parser.raw_ast.RawBlock;
import de.uni_passau.fim.se2.litterbox.ast.new_parser.raw_ast.RawBlockId;
import de.uni_passau.fim.se2.litterbox.ast.new_parser.raw_ast.RawInput;
import de.uni_passau.fim.se2.litterbox.ast.new_parser.raw_ast.RawTarget;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParserState;

final class ExpressionConverter {

    private ExpressionConverter() {
        throw new IllegalCallerException("utility class constructor");
    }

    static Expression convertExpr(
            final ProgramParserState state,
            final RawBlock.RawRegularBlock containingBlock,
            final RawInput exprBlock
    ) {
        final RawTarget target = state.getCurrentTarget();

        if (NumExprConverter.parseableAsNumExpr(target, exprBlock)) {
            return NumExprConverter.convertExpr(state, containingBlock, exprBlock);
        } else if (StringExprConverter.parseableAsStringExpr(target, exprBlock)) {
            return StringExprConverter.convertExpr(state, containingBlock, exprBlock);
        } else if (BoolExprConverter.parseableAsBoolExpr(target, exprBlock)) {
            return BoolExprConverter.convertExpr(state, containingBlock, exprBlock);
        } else if (DataExprConverter.parseableAsDataExpr(target, exprBlock)) {
            return DataExprConverter.convertExpr(state, containingBlock, exprBlock);
        } else {
            return new UnspecifiedExpression();
        }
    }

    static Expression convertExprStmt(
            final ProgramParserState state,
            final RawBlock.ArrayBlock block
    ) {
        throw new UnsupportedOperationException("todo: convert expression stmts");
    }

    static Expression convertExprStmt(
            final ProgramParserState state,
            final RawBlockId blockId,
            final RawBlock.RawRegularBlock block
    ) {
        throw new UnsupportedOperationException("todo: convert expression stmts");
    }
}
