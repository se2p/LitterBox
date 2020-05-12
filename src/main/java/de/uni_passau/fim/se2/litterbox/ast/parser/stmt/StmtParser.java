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
package de.uni_passau.fim.se2.litterbox.ast.parser.stmt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import de.uni_passau.fim.se2.litterbox.ast.Constants;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.UnspecifiedStmt;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.*;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

public class StmtParser {

    public static Stmt parse(String blockID, JsonNode blocks) throws ParsingException {
        Preconditions.checkNotNull(blockID);
        Preconditions.checkNotNull(blocks);
        Preconditions.checkState(blocks.has(blockID), "No block for id %s", blockID);

        JsonNode current = blocks.get(blockID);
        if (current instanceof ArrayNode) {
            return ExpressionStmtParser.parse(current, blocks);
        } else {
            final String opcode = current.get(Constants.OPCODE_KEY).asText();

            if (TerminationStmtOpcode.contains(opcode)) {
                if (!(current.get(Constants.FIELDS_KEY).has("STOP_OPTION")
                        && (current.get(Constants.FIELDS_KEY).get("STOP_OPTION").get(Constants.FIELD_VALUE).asText()
                        .equals("other scripts in sprite")
                        || current.get(Constants.FIELDS_KEY).get("STOP_OPTION").get(Constants.FIELD_VALUE).asText()
                        .equals("other scripts in stage")))) {
                    return TerminationStmtParser.parseTerminationStmt(blockID, current, blocks);
                }
            }

            if (ActorLookStmtOpcode.contains(opcode)) {
                return ActorLookStmtParser.parse(current, blocks);
            } else if (ControlStmtOpcode.contains(opcode)) {
                return ControlStmtParser.parse(blockID, current, blocks);
            } else if (BoolExprOpcode.contains(opcode) || NumExprOpcode.contains(opcode) || StringExprOpcode
                    .contains(opcode)) {
                return ExpressionStmtParser.parse(current, blocks);
            } else if (CommonStmtOpcode.contains(opcode)) {
                return CommonStmtParser.parse(blockID, current, blocks);
            } else if (SpriteMotionStmtOpcode.contains(opcode)) {
                return SpriteMotionStmtParser.parse(blockID, current, blocks);
            } else if (SpriteLookStmtOpcode.contains(opcode)) {
                return SpriteLookStmtParser.parse(blockID, current, blocks);
            } else if (ActorSoundStmtOpcode.contains(opcode)) {
                return ActorSoundStmtParser.parse(blockID, current, blocks);
            } else if (CallStmtOpcode.contains(opcode)) {
                return CallStmtParser.parse(blockID, current, blocks);
            } else if (ListStmtOpcode.contains(opcode)) {
                return ListStmtParser.parse(blockID, current, blocks);
            } else if (SetStmtOpcode.contains(opcode)) {
                return SetStmtParser.parse(blockID, current, blocks);
            } else if (PenOpcode.contains(opcode)) {
                return PenStmtParser.parse(blockID, current, blocks);
            } else {
                return new UnspecifiedStmt();
            }
        }
    }
}
