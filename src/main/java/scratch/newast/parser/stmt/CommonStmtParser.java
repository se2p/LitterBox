package scratch.newast.parser.stmt;

import static scratch.newast.Constants.FIELDS_KEY;
import static scratch.newast.Constants.FIELD_VALUE;
import static scratch.newast.Constants.INPUTS_KEY;
import static scratch.newast.Constants.OPCODE_KEY;
import static scratch.newast.Constants.POS_INPUT_VALUE;
import static scratch.newast.Constants.VARIABLE_KEY;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Preconditions;
import scratch.newast.Constants;
import scratch.newast.ParsingException;
import scratch.newast.model.Message;
import scratch.newast.model.expression.Expression;
import scratch.newast.model.expression.bool.BoolExpr;
import scratch.newast.model.expression.bool.UnspecifiedBoolExpr;
import scratch.newast.model.expression.num.NumExpr;
import scratch.newast.model.statement.common.Broadcast;
import scratch.newast.model.statement.common.BroadcastAndWait;
import scratch.newast.model.statement.common.ChangeVariableBy;
import scratch.newast.model.statement.common.CommonStmt;
import scratch.newast.model.statement.common.CreateCloneOf;
import scratch.newast.model.statement.common.ResetTimer;
import scratch.newast.model.statement.common.StopOtherScriptsInSprite;
import scratch.newast.model.statement.common.WaitSeconds;
import scratch.newast.model.statement.common.WaitUntil;
import scratch.newast.model.variable.Identifier;
import scratch.newast.opcodes.CommonStmtOpcode;
import scratch.newast.parser.ExpressionParser;

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
            .checkArgument(CommonStmtOpcode.contains(opcodeString), "Given blockID does not point to an event block.");

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
                stmt = parseCreateCloneOf(current,allBlocks);
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
            default:
                throw new RuntimeException("Not Implemented yet");
        }
    }

    private static CommonStmt parseChangeVariableBy(JsonNode current, JsonNode allBlocks) throws ParsingException {
        Expression stringExpr = ExpressionParser.parseExpression(current, 0, allBlocks);

        String variableName = current.get(FIELDS_KEY).get(VARIABLE_KEY).get(FIELD_VALUE).asText();
        Identifier ident = new Identifier(variableName);

        return new ChangeVariableBy(ident, stringExpr);
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
            BoolExpr boolExpr = ExpressionParser.parseBoolExpr(inputs, 0, allBlocks);
            return new WaitUntil(boolExpr);
        } else {
            return new WaitUntil(new UnspecifiedBoolExpr());
        }
    }

    private static WaitSeconds parseWaitSeconds(JsonNode current, JsonNode allBlocks) throws ParsingException {
        JsonNode inputs = current.get(INPUTS_KEY);
        NumExpr numExpr = ExpressionParser.parseNumExpr(inputs, 0, allBlocks);
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
