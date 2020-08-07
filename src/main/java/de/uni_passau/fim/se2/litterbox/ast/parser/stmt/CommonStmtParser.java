/*
 * Copyright (C) 2020 LitterBox contributors
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
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.*;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.CloneOfMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.*;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.CommonStmtOpcode;
import de.uni_passau.fim.se2.litterbox.ast.parser.*;
import de.uni_passau.fim.se2.litterbox.ast.parser.metadata.BlockMetadataParser;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.VariableInfo;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;
import java.util.List;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;

public class CommonStmtParser {

    private static final String STOP_OPTION = "STOP_OPTION";
    private static final String STOP_OTHER = "other scripts in sprite";
    private static final String STOP_OTHER_IN_STAGE = "other scripts in stage";

    /**
     * Parses a CommonStmt for a given block id.
     *
     * @param blockId   of the block to be parsed
     * @param current   JsonNode the contains the CommonStmt
     * @param allBlocks of this program
     * @return the parsed CommonStmt
     * @throws ParsingException if the block cannot be parsed into an CommonStmt
     */
    public static CommonStmt parse(String blockId, JsonNode current, JsonNode allBlocks) throws ParsingException {
        Preconditions.checkNotNull(current);
        Preconditions.checkNotNull(allBlocks);

        final String opcodeString = current.get(OPCODE_KEY).asText();
        Preconditions
                .checkArgument(CommonStmtOpcode.contains(opcodeString), "Given blockID does not point to a common "
                        + "block.");

        final CommonStmtOpcode opcode = CommonStmtOpcode.valueOf(opcodeString);
        BlockMetadata metadata = BlockMetadataParser.parse(blockId, current);
        switch (opcode) {
            case control_wait:
                return parseWaitSeconds(current, allBlocks, metadata);

            case control_wait_until:
                return parseWaitUntil(current, allBlocks, metadata);

            case control_stop:
                return parseControlStop(current, metadata);

            case control_create_clone_of:
                return parseCreateCloneOf(current, allBlocks, metadata);

            case event_broadcast:
                return parseBroadcast(current, allBlocks, metadata);

            case event_broadcastandwait:
                return parseBroadcastAndWait(current, allBlocks, metadata);

            case sensing_resettimer:
                return new ResetTimer(metadata);

            case data_changevariableby:
                return parseChangeVariableBy(current, allBlocks, metadata);

            default:
                throw new RuntimeException("Not Implemented yet");
        }
    }

    private static CommonStmt parseChangeVariableBy(JsonNode current, JsonNode allBlocks, BlockMetadata metadata)
            throws ParsingException {

        Expression numExpr = NumExprParser.parseNumExpr(current, VALUE_KEY, allBlocks);
        Identifier var;
        String variableName = current.get(FIELDS_KEY).get(VARIABLE_KEY).get(VARIABLE_NAME_POS).asText();
        String variableId = current.get(FIELDS_KEY).get(VARIABLE_KEY).get(VARIABLE_IDENTIFIER_POS).asText();
        String currentActorName = ActorDefinitionParser.getCurrentActor().getName();
        if (ProgramParser.symbolTable.getVariable(variableId, variableName, currentActorName).isEmpty()) {
            var = new UnspecifiedId();
        } else {
            VariableInfo variableInfo
                    = ProgramParser.symbolTable.getVariable(variableId, variableName, currentActorName).get();
            String actorName = variableInfo.getActor();
            var = new Qualified(new StrId(actorName), new Variable(new StrId(variableName)));
        }

        return new ChangeVariableBy(var, numExpr, metadata);
    }

    private static CommonStmt parseBroadcast(JsonNode current, JsonNode allBlocks, BlockMetadata metadata)
            throws ParsingException {
        Preconditions.checkArgument(current.get(INPUTS_KEY).get(BROADCAST_INPUT_KEY).isArray());

        // The inputs contains array itself,
        StringExpr messageName = StringExprParser.parseStringExpr(current, BROADCAST_INPUT_KEY, allBlocks);

        Message message = new Message(messageName);
        return new Broadcast(message, metadata);
    }

    private static CommonStmt parseBroadcastAndWait(JsonNode current, JsonNode allBlocks, BlockMetadata metadata)
            throws ParsingException {

        Preconditions.checkArgument(current.get(INPUTS_KEY).get(BROADCAST_INPUT_KEY).isArray());

        // The inputs contains array itself,
        StringExpr messageName = StringExprParser.parseStringExpr(current, BROADCAST_INPUT_KEY, allBlocks);

        Message message = new Message(messageName);
        BroadcastAndWait broadcast = new BroadcastAndWait(message, metadata);
        return broadcast;
    }

    private static CommonStmt parseCreateCloneOf(JsonNode current, JsonNode allBlocks, BlockMetadata metadata)
            throws ParsingException {
        JsonNode inputs = current.get(INPUTS_KEY);
        List<JsonNode> inputsList = new ArrayList<>();
        inputs.elements().forEachRemaining(inputsList::add);

        if (getShadowIndicator((ArrayNode) inputsList.get(0)) == 1) {
            String cloneOptionMenu = inputs.get(CLONE_OPTION).get(POS_INPUT_VALUE).asText();
            JsonNode optionBlock = allBlocks.get(cloneOptionMenu);
            BlockMetadata cloneMenuMetadata = BlockMetadataParser.parse(cloneOptionMenu, optionBlock);
            String cloneValue = optionBlock.get(FIELDS_KEY).get(CLONE_OPTION).get(FIELD_VALUE).asText();
            LocalIdentifier ident = new StrId(cloneValue);
            return new CreateCloneOf(new AsString(ident), new CloneOfMetadata(metadata, cloneMenuMetadata));
        } else {
            final StringExpr stringExpr = StringExprParser.parseStringExpr(current, CLONE_OPTION, allBlocks);
            return new CreateCloneOf(stringExpr, new CloneOfMetadata(metadata, new NoBlockMetadata()));
        }
    }

    private static WaitUntil parseWaitUntil(JsonNode current, JsonNode allBlocks, BlockMetadata metadata)
            throws ParsingException {
        JsonNode inputs = current.get(INPUTS_KEY);
        if (inputs.has(CONDITION_KEY)) {
            BoolExpr boolExpr = BoolExprParser.parseBoolExpr(current, CONDITION_KEY, allBlocks);
            return new WaitUntil(boolExpr, metadata);
        } else {
            return new WaitUntil(new UnspecifiedBoolExpr(), metadata);
        }
    }

    private static WaitSeconds parseWaitSeconds(JsonNode current, JsonNode allBlocks, BlockMetadata metadata)
            throws ParsingException {
        NumExpr numExpr = NumExprParser.parseNumExpr(current, DURATION_KEY, allBlocks);
        return new WaitSeconds(numExpr, metadata);
    }

    private static CommonStmt parseControlStop(JsonNode current, BlockMetadata metadata) {
        CommonStmt stmt;
        String stopOptionValue =
                current.get(Constants.FIELDS_KEY).get(STOP_OPTION).get(Constants.FIELD_VALUE)
                        .asText();

        if (stopOptionValue.equals(STOP_OTHER) || stopOptionValue.equals(STOP_OTHER_IN_STAGE)) {
            stmt = new StopOtherScriptsInSprite(metadata);
        } else {
            throw new RuntimeException();
        }

        return stmt;
    }

    static int getShadowIndicator(ArrayNode exprArray) {
        return exprArray.get(Constants.POS_INPUT_SHADOW).asInt();
    }
}
