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
import de.uni_passau.fim.se2.litterbox.ast.model.Message;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.UnspecifiedBoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AsString;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.StringExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.*;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.*;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.CommonStmtOpcode;
import de.uni_passau.fim.se2.litterbox.ast.parser.BoolExprParser;
import de.uni_passau.fim.se2.litterbox.ast.parser.NumExprParser;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParser;
import de.uni_passau.fim.se2.litterbox.ast.parser.StringExprParser;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.VariableInfo;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;
import java.util.List;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;

public class CommonStmtParser {

    private static final String CLONE_OPTION = "CLONE_OPTION";
    private static final String STOP_OPTION = "STOP_OPTION";
    private static final String STOP_OTHER = "other scripts in sprite";
    private static final String STOP_OTHER_IN_STAGE = "other scripts in stage";
    private static final String BROADCAST_INPUT_KEY = "BROADCAST_INPUT";

    public static CommonStmt parse(JsonNode current, JsonNode allBlocks) throws ParsingException {
        Preconditions.checkNotNull(current);
        Preconditions.checkNotNull(allBlocks);

        final String opcodeString = current.get(OPCODE_KEY).asText();
        Preconditions
                .checkArgument(CommonStmtOpcode.contains(opcodeString), "Given blockID does not point to a common " +
                        "block.");

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

        default:
            throw new RuntimeException("Not Implemented yet");
        }
    }



    private static CommonStmt parseChangeVariableBy(JsonNode current, JsonNode allBlocks) throws ParsingException {
        Expression numExpr = NumExprParser.parseNumExpr(current, 0, allBlocks);
        Variable var;
        String variableName = current.get(FIELDS_KEY).get(VARIABLE_KEY).get(VARIABLE_NAME_POS).asText();
        String variableID = current.get(FIELDS_KEY).get(VARIABLE_KEY).get(VARIABLE_IDENTIFIER_POS).asText();
        if (!ProgramParser.symbolTable.getVariables().containsKey(variableID)) {
            var = new UnspecifiedId();
        } else {
            VariableInfo variableInfo = ProgramParser.symbolTable.getVariables().get(variableID);
            String actorName = variableInfo.getActor();
            var = new Qualified(new StrId(actorName), new StrId(VARIABLE_ABBREVIATION + variableName));
        }

        return new ChangeVariableBy(var, numExpr);
    }

    private static CommonStmt parseBroadcast(JsonNode current, JsonNode allBlocks) throws ParsingException {
        Preconditions.checkArgument(current.get(INPUTS_KEY).get(BROADCAST_INPUT_KEY).isArray());

        // The inputs contains array itself,
        StringExpr messageName = StringExprParser.parseStringExpr(current, BROADCAST_INPUT_KEY, allBlocks);

        Message message = new Message(messageName);
        return new Broadcast(message);
    }

    private static CommonStmt parseBroadcastAndWait(JsonNode current, JsonNode allBlocks) throws ParsingException {
        Preconditions.checkArgument(current.get(INPUTS_KEY).get(BROADCAST_INPUT_KEY).isArray());

        // The inputs contains array itself,
        StringExpr messageName = StringExprParser.parseStringExpr(current, BROADCAST_INPUT_KEY, allBlocks);

        Message message = new Message(messageName);
        BroadcastAndWait broadcast = new BroadcastAndWait(message);
        return broadcast;
    }

    private static CommonStmt parseCreateCloneOf(JsonNode current, JsonNode allBlocks) throws ParsingException {
        JsonNode inputs = current.get(INPUTS_KEY);
        List<JsonNode> inputsList = new ArrayList<>();
        inputs.elements().forEachRemaining(inputsList::add);

        if (getShadowIndicator((ArrayNode) inputsList.get(0)) == 1) {
            String cloneOptionMenu = inputs.get(CLONE_OPTION).get(Constants.POS_INPUT_VALUE).asText();
            JsonNode optionBlock = allBlocks.get(cloneOptionMenu);
            String cloneValue = optionBlock.get(FIELDS_KEY).get(CLONE_OPTION).get(FIELD_VALUE).asText();
            Identifier ident = new StrId(cloneValue);
            return new CreateCloneOf(new AsString(ident));
        } else {
            final StringExpr stringExpr = StringExprParser.parseStringExpr(current, 0, allBlocks);
            return new CreateCloneOf(stringExpr);
        }
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

        if (stopOptionValue.equals(STOP_OTHER) || stopOptionValue.equals(STOP_OTHER_IN_STAGE)) {
            stmt = new StopOtherScriptsInSprite();
        } else {
            throw new RuntimeException();
        }

        return stmt;
    }

    static int getShadowIndicator(ArrayNode exprArray) {
        return exprArray.get(Constants.POS_INPUT_SHADOW).asInt();
    }
}
