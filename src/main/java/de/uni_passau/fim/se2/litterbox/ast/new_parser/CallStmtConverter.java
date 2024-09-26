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
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.UnspecifiedBoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.list.ExpressionList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.UnspecifiedStringExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.CallStmt;
import de.uni_passau.fim.se2.litterbox.ast.new_parser.raw_ast.*;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParserState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class CallStmtConverter extends StmtConverter<CallStmt> {
    CallStmtConverter(ProgramParserState state) {
        super(state);
    }

    @Override
    CallStmt convertStmt(final RawBlockId blockId, final RawBlock.RawRegularBlock block) {
        final RawMutation mutation = block.mutation()
                .orElseThrow(() -> new InternalParsingException("CallStmt is incomplete."));
        final List<RawBlockId> argumentIds = mutation.argumentids();

        final List<Expression> expressions = new ArrayList<>();

        for (final RawBlockId argument : argumentIds) {
            final RawInput input = block.inputs().get(argument.id());

            if (input != null) {
                final Expression expr = ExprConverter.convertExpr(state, block, input);
                if (expr instanceof UnspecifiedStringExpr) {
                    // to make it identical to the old parser
                    expressions.add(new UnspecifiedExpression());
                } else {
                    expressions.add(expr);
                }
            } else {
                expressions.add(new UnspecifiedBoolExpr());
            }
        }

        final StrId callStmtId = getCallStmtId(mutation);
        return new CallStmt(
                callStmtId,
                new ExpressionList(Collections.unmodifiableList(expressions)),
                RawBlockMetadataConverter.convertBlockMetadata(blockId, block)
        );
    }

    private StrId getCallStmtId(final RawMutation callBlockMutation) {
        // %n parameters probably only exist when converting from Scratch 2 programs. At the same time, Scratch converts
        // procedures to have %s parameters instead, though. Therefore, the matching by name is broken in those cases.
        // => convert all number (%n) parameter types to number/text (%s) parameter types, too
        final String identifier = callBlockMutation.proccode().replace("%n", "%s");

        return new StrId(identifier);
    }
}
