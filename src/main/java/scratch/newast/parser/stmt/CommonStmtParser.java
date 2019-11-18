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
package scratch.newast.parser.stmt;

import static scratch.newast.Constants.FIELDS_KEY;
import static scratch.newast.Constants.FIELD_VALUE;
import static scratch.newast.Constants.INPUTS_KEY;
import static scratch.newast.Constants.OPCODE_KEY;
import static scratch.newast.Constants.POS_INPUT_VALUE;
import static scratch.newast.Constants.VARIABLE_KEY;
import static scratch.newast.opcodes.CommonStmtOpcode.looks_changeeffectby;
import static scratch.newast.opcodes.CommonStmtOpcode.sound_changeeffectby;
import static scratch.newast.opcodes.CommonStmtOpcode.sound_changevolumeby;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Preconditions;
import scratch.newast.Constants;
import scratch.newast.ParsingException;
import scratch.newast.model.Message;
import scratch.newast.model.expression.Expression;
import scratch.newast.model.expression.bool.BoolExpr;
import scratch.newast.model.expression.bool.UnspecifiedBoolExpr;
import scratch.newast.model.expression.num.NumExpr;
import scratch.newast.model.expression.string.Str;
import scratch.newast.model.statement.common.Broadcast;
import scratch.newast.model.statement.common.BroadcastAndWait;
import scratch.newast.model.statement.common.ChangeAttributeBy;
import scratch.newast.model.statement.common.ChangeVariableBy;
import scratch.newast.model.statement.common.CommonStmt;
import scratch.newast.model.statement.common.CreateCloneOf;
import scratch.newast.model.statement.common.ResetTimer;
import scratch.newast.model.statement.common.StopOtherScriptsInSprite;
import scratch.newast.model.statement.common.WaitSeconds;
import scratch.newast.model.statement.common.WaitUntil;
import scratch.newast.model.variable.Identifier;
import scratch.newast.opcodes.CommonStmtOpcode;
import scratch.newast.parser.BoolExprParser;
import scratch.newast.parser.NumExprParser;

public class CommonStmtParser {

    private static final String CLONE_OPTION = "CLONE_OPTION";
    private static final String STOP_OPTION = "STOP_OPTION";
    private static final String STOP_OTHER = "other scripts in sprite";
    private static final String BROADCAST_INPUT_KEY = "BROADCAST_INPUT";

    public static CommonStmt parse(JsonNode current, JsonNode allBlocks) throws ParsingException {
        Preconditions.checkNotNull(current);
        Preconditions.checkNotNull(allBlocks);

        String opcodeString = current.get(OPCODE_KEY).asText();
        Preconditions
            .checkArgument(CommonStmtOpcode.contains(opcodeString), "Given blockID does not point to a common block.");

        CommonStmtOpcode opcode = CommonStmtOpcode.valueOf(opcodeString);
        CommonStmt stmt;

        switch (opcode) {
            case control_wait:
                stmt = parseWaitSeconds(current, allBlocks);
                return stmt;
            case control_wait_until:
                stmt = parseWaitUntil(current, allBlocks);
                return stmt;
            case control_stop:
                stmt = parseControlStop(current);
                return stmt;
            case control_create_clone_of:
                stmt = parseCreateCloneOf(current, allBlocks);
                return stmt;
            case event_broadcast:
                stmt = parseBroadcast(current, allBlocks);
                return stmt;
            case event_broadcastandwait:
                stmt = parseBroadcastAndWait(current, allBlocks);
                return stmt;
            case sensing_resettimer:
                stmt = new ResetTimer();
                return stmt;
            case data_changevariableby:
                stmt = parseChangeVariableBy(current, allBlocks);
                return stmt;

            case sound_changevolumeby:
            case sound_changeeffectby:
                //case looks_changesizeby: //FIXME is this now a common stmt?
            case looks_changeeffectby:
                stmt = parseChangeAttributeBy(current, allBlocks);
                return stmt;
            default:
                throw new RuntimeException("Not Implemented yet");
        }
    }

    private static CommonStmt parseChangeAttributeBy(JsonNode current, JsonNode allBlocks) throws ParsingException {
        String opcodeString = current.get(OPCODE_KEY).asText();
        CommonStmtOpcode opcode = CommonStmtOpcode.valueOf(opcodeString);
        CommonStmt stmt;

        if (sound_changevolumeby.equals(opcode)) {
            String attributeName = "VOLUME";
            NumExpr numExpr = NumExprParser.parseNumExpr(current, 0,
                allBlocks);
            return new ChangeAttributeBy(new Str(attributeName), numExpr);
        } else if (sound_changeeffectby.equals(opcode) || looks_changeeffectby.equals(opcode)) {
            NumExpr numExpr = NumExprParser.parseNumExpr(current, 0, allBlocks);
            String effectName = current.get(FIELDS_KEY).get("EFFECT").get(0).asText();
            return new ChangeAttributeBy(new Str(effectName), numExpr);
//        } else if (looks_changesizeby.equals(opcode)) {
        } else {
            throw new ParsingException("Cannot parse block with opcode " + opcodeString + " to ChangeAttributeBy");
        }
    }

    private static CommonStmt parseChangeVariableBy(JsonNode current, JsonNode allBlocks) throws ParsingException {
        Expression numExpr = NumExprParser.parseNumExpr(current, 0, allBlocks);

        String variableName = current.get(FIELDS_KEY).get(VARIABLE_KEY).get(FIELD_VALUE).asText();
        Identifier ident = new Identifier(variableName);

        return new ChangeVariableBy(ident, numExpr);
    }

    private static CommonStmt parseBroadcast(JsonNode current, JsonNode allBlocks) {
        Preconditions.checkArgument(current.get(INPUTS_KEY).get(BROADCAST_INPUT_KEY).isArray());

        // The inputs contains array itself,
        String messageName = current.get(INPUTS_KEY).get(BROADCAST_INPUT_KEY)
            .get(Constants.POS_INPUT_VALUE)
            .get(POS_INPUT_VALUE).asText();

        Message message = new Message(messageName);
        return new Broadcast(message);
    }

    private static CommonStmt parseBroadcastAndWait(JsonNode current, JsonNode allBlocks) {
        Preconditions.checkArgument(current.get(INPUTS_KEY).get(BROADCAST_INPUT_KEY).isArray());

        // The inputs contains array itself,
        String messageName = current.get(INPUTS_KEY).get(BROADCAST_INPUT_KEY)
            .get(Constants.POS_INPUT_VALUE)
            .get(POS_INPUT_VALUE).asText();

        Message message = new Message(messageName);
        BroadcastAndWait broadcast = new BroadcastAndWait(message);
        return broadcast;
    }

    private static CommonStmt parseCreateCloneOf(JsonNode current, JsonNode allBlocks) {
        JsonNode inputs = current.get(INPUTS_KEY);
        String cloneOptionMenu = inputs.get(CLONE_OPTION).get(Constants.POS_INPUT_VALUE).asText();
        JsonNode optionBlock = allBlocks.get(cloneOptionMenu);
        String cloneValue = optionBlock.get(FIELDS_KEY).get(CLONE_OPTION).get(FIELD_VALUE).asText();
        Identifier ident = new Identifier(cloneValue);
        return new CreateCloneOf(ident);
    }

    private static WaitUntil parseWaitUntil(JsonNode current, JsonNode allBlocks) throws ParsingException {
        JsonNode inputs = current.get(INPUTS_KEY);
        if (inputs.elements().hasNext()) {
            BoolExpr boolExpr = BoolExprParser.parseBoolExpr(current, 0, allBlocks);
            return new WaitUntil(boolExpr);
        } else {
            return new WaitUntil(new UnspecifiedBoolExpr());
        }
    }

    private static WaitSeconds parseWaitSeconds(JsonNode current, JsonNode allBlocks) throws ParsingException {
        NumExpr numExpr = NumExprParser.parseNumExpr(current, 0, allBlocks);
        return new WaitSeconds(numExpr);
    }

    private static CommonStmt parseControlStop(JsonNode current) throws ParsingException {
        CommonStmt stmt;
        String stopOptionValue =
            current.get(Constants.FIELDS_KEY).get(STOP_OPTION).get(Constants.FIELD_VALUE)
                .asText();

        if (stopOptionValue.equals(STOP_OTHER)) {
            stmt = new StopOtherScriptsInSprite();
        } else {
            throw new RuntimeException();
        }

        return stmt;
    }
}
