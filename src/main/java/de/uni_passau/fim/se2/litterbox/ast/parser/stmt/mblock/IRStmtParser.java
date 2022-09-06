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
package de.uni_passau.fim.se2.litterbox.ast.parser.stmt.mblock;

import com.fasterxml.jackson.databind.JsonNode;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.StringExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.ir.IRStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.ir.LearnWithTime;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.ir.SendIR;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.ir.SendLearnResult;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.mblock.IRStmtOpcode;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParserState;
import de.uni_passau.fim.se2.litterbox.ast.parser.StringExprParser;
import de.uni_passau.fim.se2.litterbox.ast.parser.metadata.BlockMetadataParser;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;

public class IRStmtParser {

    public static IRStmt parse(final ProgramParserState state, String blockId, JsonNode current, JsonNode blocks) throws ParsingException {
        Preconditions.checkNotNull(blockId);
        Preconditions.checkNotNull(current);
        Preconditions.checkNotNull(blocks);

        String opcodeString = current.get(OPCODE_KEY).asText();
        Preconditions
                .checkArgument(IRStmtOpcode.contains(opcodeString), "Given blockId does not point to an Reset block.");

        IRStmtOpcode opcode = IRStmtOpcode.getOpcode(opcodeString);
        BlockMetadata metadata = BlockMetadataParser.parse(blockId, current);
        switch (opcode) {
            case comm_send_ir:
                StringExpr text = StringExprParser.parseStringExpr(state, current, STRING_KEY, blocks);
                return new SendIR(text, metadata);

            case comm_learn_with_time:
                return new LearnWithTime(metadata);

            case comm_send_learn_result:
                return new SendLearnResult(metadata);

            case send_ir:
                text = StringExprParser.parseStringExpr(state, current, MESSAGE_KEY, blocks);
                return new SendIR(text, metadata);

            default:
                throw new IllegalStateException("Reset Block with opcode " + opcode + " was not parsed");
        }
    }
}
