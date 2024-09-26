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
package de.uni_passau.fim.se2.litterbox.ast.parser;

import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.StringExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.ir.IRStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.ir.LearnWithTime;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.ir.SendIR;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.ir.SendLearnResult;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.mblock.IRStmtOpcode;
import de.uni_passau.fim.se2.litterbox.ast.parser.raw_ast.KnownInputs;
import de.uni_passau.fim.se2.litterbox.ast.parser.raw_ast.RawBlock;
import de.uni_passau.fim.se2.litterbox.ast.parser.raw_ast.RawBlockId;

final class IrStmtConverter extends StmtConverter<IRStmt> {

    IrStmtConverter(final ProgramParserState state) {
        super(state);
    }

    @Override
    IRStmt convertStmt(final RawBlockId blockId, final RawBlock.RawRegularBlock block) {
        final IRStmtOpcode opcode = IRStmtOpcode.getOpcode(block.opcode());
        final BlockMetadata metadata = RawBlockMetadataConverter.convertBlockMetadata(blockId, block);

        return switch (opcode) {
            case comm_learn_with_time -> new LearnWithTime(metadata);
            case comm_send_learn_result -> new SendLearnResult(metadata);
            case comm_send_ir -> {
                final StringExpr text = StringExprConverter.convertStringExpr(state, block, KnownInputs.STRING);
                yield new SendIR(text, metadata);
            }
            case send_ir -> {
                final StringExpr text = StringExprConverter.convertStringExpr(state, block, KnownInputs.MESSAGE);
                yield new SendIR(text, metadata);
            }
        };
    }
}
