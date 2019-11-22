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
import scratch.ast.Constants;
import scratch.ast.ParsingException;
import scratch.ast.model.Message;
import scratch.ast.model.expression.Expression;
import scratch.ast.model.expression.bool.BoolExpr;
import scratch.ast.model.expression.bool.UnspecifiedBoolExpr;
import scratch.ast.model.expression.num.NumExpr;
import scratch.ast.model.literals.StringLiteral;
import scratch.ast.model.statement.common.*;
import scratch.ast.model.variable.Identifier;
import scratch.ast.model.variable.StrId;
import scratch.ast.opcodes.CommonStmtOpcode;
import scratch.ast.parser.BoolExprParser;
import scratch.ast.parser.NumExprParser;
import scratch.utils.Preconditions;

import static scratch.ast.Constants.*;
import static scratch.ast.opcodes.CommonStmtOpcode.*;

public class CommonStmtParser {

    private static final String CLONE_OPTION = "CLONE_OPTION";
    private static final String STOP_OPTION = "STOP_OPTION";
    private static final String STOP_OTHER = "other scripts in sprite";
    private static final String BROADCAST_INPUT_KEY = "BROADCAST_INPUT";

    public static CommonStmt parse(JsonNode current, JsonNode allBlocks) throws ParsingException {
        Preconditions.checkNotNull(current);
        Preconditions.checkNotNull(allBlocks);

        final String opcodeString = current.get(OPCODE_KEY).asText();
        Preconditions
            .checkArgument(CommonStmtOpcode.contains(opcodeString), "Given blockID does not point to a common block.");

        final CommonStmtOpcode opcode = CommonStmtOpcode.valueOf(opcodeString);

        switch (opcode) {
            case control_wait:
                return parseWaitSeconds(current, allBlocks);

            case control_wait_until:
                return parseWaitUntil(current, allBlocks);

            case control_stop:
                return parseControlStop(current);

            case control_create_clone_of:
                return parseCreateCloneOf(current, allBlocks);

            case event_broadcast:
                return parseBroadcast(current, allBlocks);

            case event_broadcastandwait:
                return parseBroadcastAndWait(current, allBlocks);

            case sensing_resettimer:
                return new ResetTimer();

            case data_changevariableby:
                return parseChangeVariableBy(current, allBlocks);

            case sound_changevolumeby:
            case sound_changeeffectby:
            case looks_changeeffectby:
                return parseChangeAttributeBy(current, allBlocks);

            default:
                throw new RuntimeException("Not Implemented yet");
        }
    }

    private static CommonStmt parseChangeAttributeBy(JsonNode current, JsonNode allBlocks) throws ParsingException {
        String opcodeString = current.get(OPCODE_KEY).asText();
        CommonStmtOpcode opcode = CommonStmtOpcode.valueOf(opcodeString);

        if (sound_changevolumeby.equals(opcode)) {
            String attributeName = "VOLUME";
            NumExpr numExpr = NumExprParser.parseNumExpr(current, 0,
                allBlocks);
            return new ChangeAttributeBy(new StringLiteral(attributeName), numExpr);

        } else if (sound_changeeffectby.equals(opcode) || looks_changeeffectby.equals(opcode)) {
            NumExpr numExpr = NumExprParser.parseNumExpr(current, 0, allBlocks);
            String effectName = current.get(FIELDS_KEY).get("EFFECT").get(0).asText();
            return new ChangeAttributeBy(new StringLiteral(effectName), numExpr);

//        } else if (looks_changesizeby.equals(opcode)) {
        } else {
            throw new ParsingException("Cannot parse block with opcode " + opcodeString + " to ChangeAttributeBy");
        }
    }

    private static CommonStmt parseChangeVariableBy(JsonNode current, JsonNode allBlocks) throws ParsingException {
        Expression numExpr = NumExprParser.parseNumExpr(current, 0, allBlocks);

        String variableName = current.get(FIELDS_KEY).get(VARIABLE_KEY).get(FIELD_VALUE).asText();
        Identifier ident = new StrId(variableName);

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
        Identifier ident = new StrId(cloneValue);
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
