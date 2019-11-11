package scratch.newast.parser.stmt;

import static scratch.newast.Constants.INPUTS_KEY;
import static scratch.newast.Constants.OPCODE_KEY;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Preconditions;
import scratch.newast.ParsingException;
import scratch.newast.model.expression.bool.BoolExpr;
import scratch.newast.model.expression.num.NumExpr;
import scratch.newast.model.statement.common.CommonStmt;
import scratch.newast.model.statement.common.WaitSeconds;
import scratch.newast.model.statement.common.WaitUntil;
import scratch.newast.opcodes.CommonStmtOpcode;
import scratch.newast.opcodes.EventOpcode;
import scratch.newast.parser.ExpressionParser;

public class CommonStmtParser {

    public static CommonStmt parse(JsonNode current, JsonNode allBlocks) throws ParsingException {
        Preconditions.checkNotNull(current);
        Preconditions.checkNotNull(allBlocks);

        String opcodeString = current.get(OPCODE_KEY).asText();
        Preconditions
            .checkArgument(EventOpcode.contains(opcodeString), "Given blockID does not point to an event block.");

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
            case control_create_clone_of:
            case event_broadcast:
            case event_broadcastandwait:
            case sensing_resettimer:
            case data_changevariableby:
            default:
                throw new RuntimeException("Not Implemented yet");
        }
    }

    private static WaitUntil parseWaitUntil(JsonNode current, JsonNode allBlocks) {
        JsonNode inputs = current.get(INPUTS_KEY);
        BoolExpr boolExpr = ExpressionParser.parseBoolExpr(inputs, 0, allBlocks);
        return new WaitUntil(boolExpr);
    }

    private static WaitSeconds parseWaitSeconds(JsonNode current, JsonNode allBlocks) throws ParsingException {
        JsonNode inputs = current.get(INPUTS_KEY);
        NumExpr numExpr = ExpressionParser.parseNumExpr(inputs, 0, allBlocks);
        return new WaitSeconds(numExpr);

    }
}
