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

import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.UnspecifiedBoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.*;
import de.uni_passau.fim.se2.litterbox.ast.new_parser.raw_ast.*;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.ControlStmtOpcode;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParserState;

import java.util.Collections;
import java.util.Optional;

final class ControlStmtConverter extends StmtConverter<ControlStmt> {

    ControlStmtConverter(final ProgramParserState state) {
        super(state);
    }

    @Override
    ControlStmt convertStmt(final RawBlockId blockId, final RawBlock.RawRegularBlock stmt) {
        final ControlStmtOpcode opcode = ControlStmtOpcode.valueOf(stmt.opcode());
        final BlockMetadata metadata = RawBlockMetadataConverter.convertBlockMetadata(blockId, stmt);

        return switch (opcode) {
            case control_if -> {
                final BoolExpr condition = getCondition(stmt);
                final StmtList stmtList = getSubstackStmtList(stmt, KnownInputs.SUBSTACK);
                yield new IfThenStmt(condition, stmtList, metadata);
            }
            case control_if_else -> {
                final BoolExpr condition = getCondition(stmt);
                final StmtList thenStmtList = getSubstackStmtList(stmt, KnownInputs.SUBSTACK);
                final StmtList elseStmtList = getSubstackStmtList(stmt, KnownInputs.SUBSTACK2);
                yield new IfElseStmt(condition, thenStmtList, elseStmtList, metadata);
            }
            case control_repeat -> {
                final NumExpr repetitions = NumExprConverter.convertNumExpr(state, stmt, KnownInputs.TIMES);
                final StmtList stmtList = getSubstackStmtList(stmt, KnownInputs.SUBSTACK);
                yield new RepeatTimesStmt(repetitions, stmtList, metadata);
            }
            case control_repeat_until -> {
                final BoolExpr condition = getCondition(stmt);
                final StmtList stmtList = getSubstackStmtList(stmt, KnownInputs.SUBSTACK);
                yield new UntilStmt(condition, stmtList, metadata);
            }
            case control_forever -> {
                final StmtList stmtList = getSubstackStmtList(stmt, KnownInputs.SUBSTACK);
                yield new RepeatForeverStmt(stmtList, metadata);
            }
        };
    }

    private StmtList getSubstackStmtList(final RawBlock.RawRegularBlock stmt, final KnownInputs inputKey) {
        final Optional<RawInput> substackInput = stmt.getOptionalInput(inputKey);

        if (substackInput.isPresent() && substackInput.get().input() instanceof BlockRef.IdRef stmtListRef) {
           return RawScriptConverter.convertStmtList(state, stmtListRef.id());
        } else {
            return new StmtList(Collections.emptyList());
        }
    }

    private BoolExpr getCondition(final RawBlock.RawRegularBlock stmt) {
        final Optional<RawInput> conditionInput = stmt.getOptionalInput(KnownInputs.CONDITION);
        if (conditionInput.isEmpty()) {
            return new UnspecifiedBoolExpr();
        } else {
            return BoolExprConverter.convertBoolExpr(state, stmt, conditionInput.get());
        }
    }
}
