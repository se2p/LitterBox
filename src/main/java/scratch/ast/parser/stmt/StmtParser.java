/*
 * Copyright (C) 2019 LitterBox contributors
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
package scratch.ast.parser.stmt;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Preconditions;
import scratch.ast.Constants;
import scratch.ast.ParsingException;
import scratch.ast.model.statement.Stmt;
import scratch.ast.opcodes.ActorLookStmtOpcode;
import scratch.ast.opcodes.ActorSoundStmtOpcode;
import scratch.ast.opcodes.BoolExprOpcode;
import scratch.ast.opcodes.CallStmtOpcode;
import scratch.ast.opcodes.CommonStmtOpcode;
import scratch.ast.opcodes.ControlStmtOpcode;
import scratch.ast.opcodes.ListStmtOpcode;
import scratch.ast.opcodes.NumExprOpcode;
import scratch.ast.opcodes.SetStmtOpcode;
import scratch.ast.opcodes.SpriteLookStmtOpcode;
import scratch.ast.opcodes.SpriteMotionStmtOpcode;
import scratch.ast.opcodes.StringExprOpcode;
import scratch.ast.opcodes.TerminationStmtOpcode;

public class StmtParser {

    public static Stmt parse(String blockID, JsonNode blocks) throws ParsingException {
        Preconditions.checkNotNull(blockID);
        Preconditions.checkNotNull(blocks);
        Preconditions.checkState(blocks.has(blockID), "No block for id %s", blockID);

        JsonNode current = blocks.get(blockID);
        String opcode = current.get(Constants.OPCODE_KEY).asText();

        Stmt stmt;
        if (TerminationStmtOpcode.contains(opcode)) {
            // FIXME Control_Stop is also a CommonStmt
            stmt = TerminationStmtParser.parseTerminationStmt(current, blocks);
            return stmt;
        } else if (ActorLookStmtOpcode.contains(opcode)) {
            stmt = ActorLookStmtParser.parse(current, blocks);
            return stmt;
        } else if (ControlStmtOpcode.contains(opcode)) {
            stmt = ControlStmtParser.parse(current, blocks);
            return stmt;
        } else if (BoolExprOpcode.contains(opcode) || NumExprOpcode.contains(opcode) || StringExprOpcode
            .contains(opcode)) {
            stmt = ExpressionStmtParser.parse(current, blocks);
            return stmt;
        } else if (CommonStmtOpcode.contains(opcode)) {
            stmt = CommonStmtParser.parse(current, blocks);
            return stmt;
        } else if (SpriteMotionStmtOpcode.contains(opcode)) {
            stmt = SpriteMotionStmtParser.parse(current, blocks);
            return stmt;
        } else if (SpriteLookStmtOpcode.contains(opcode)) {
            stmt = SpriteLookStmtParser.parse(current, blocks);
            return stmt;
        } else if (ActorSoundStmtOpcode.contains(opcode)) {
            stmt = ActorSoundStmtParser.parse(current, blocks);
            return stmt;
        } else if (CallStmtOpcode.contains(opcode)) {
            stmt = CallStmtParser.parse(current, blockID, blocks);
            return stmt;
        } else if (ListStmtOpcode.contains(opcode)) {
            stmt = ListStmtParser.parse(current, blocks);
            return stmt;
        } else if (SetStmtOpcode.contains(opcode)) {
            stmt = SetStmtParser.parse(current, blocks);
            return stmt;
        }

        throw new RuntimeException("Not implemented");
    }
}
