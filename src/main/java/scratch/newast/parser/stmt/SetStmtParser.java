package scratch.newast.parser.stmt;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Preconditions;
import scratch.newast.model.expression.Expression;
import scratch.newast.model.expression.string.Str;
import scratch.newast.model.statement.Stmt;
import scratch.newast.model.statement.common.SetAttributeTo;
import scratch.newast.model.statement.common.SetStmt;
import scratch.newast.opcodes.SetStmtOpcode;
import scratch.newast.parser.ExpressionParser;

import static scratch.newast.Constants.*;

public class SetStmtParser {
    public static Stmt parse(JsonNode current, JsonNode allBlocks) {
        Preconditions.checkNotNull(current);
        Preconditions.checkNotNull(allBlocks);

        String opcodeString = current.get(OPCODE_KEY).asText();
        Preconditions
                .checkArgument(SetStmtOpcode.contains(opcodeString), "Given blockID does not point to a set block.");

        SetStmtOpcode opcode =SetStmtOpcode.valueOf(opcodeString);
        SetStmt stmt;

        switch (opcode) {
            case data_setvariableto:
                stmt = parseSetVariable(current, allBlocks);
                return stmt;
            case sensing_setdragmode:
                stmt = parseSetDragmode(current, allBlocks);
                return stmt;
            case motion_setrotationstyle:
                stmt = parseSetRotationStyle(current);
                return stmt;
            case looks_seteffectto:
                stmt = parseSetLookEffect(current,allBlocks);
                return stmt;
            case sound_seteffectto:
                stmt = parseSetSoundEffect(current, allBlocks);
                return stmt;
            case sound_setvolumeto:
                stmt = parseSetVolumeTo(current, allBlocks);
                return stmt;
            default:
                throw new RuntimeException("Not Implemented yet");
        }
    }

    private static SetStmt parseSetVolumeTo(JsonNode current, JsonNode allBlocks) {
        return new SetAttributeTo(new Str(VOLUME_KEY), ExpressionParser.parseExpression(current.get(INPUTS_KEY),0,allBlocks));
    }

    private static SetStmt parseSetSoundEffect(JsonNode current, JsonNode allBlocks) {
        //new SetAttributeTo(new Str())
        throw new RuntimeException("not implemented");
    }

    private static SetStmt parseSetLookEffect(JsonNode current, JsonNode allBlocks) {throw new RuntimeException("not implemented");
    }

    private static SetStmt parseSetRotationStyle(JsonNode current) {throw new RuntimeException("not implemented");
    }

    private static SetStmt parseSetDragmode(JsonNode current, JsonNode allBlocks) {throw new RuntimeException("not implemented");
    }

    private static SetStmt parseSetVariable(JsonNode current, JsonNode allBlocks) {throw new RuntimeException("not implemented");
    }
}
