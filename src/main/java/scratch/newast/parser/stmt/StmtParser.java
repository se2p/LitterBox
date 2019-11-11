package scratch.newast.parser.stmt;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Preconditions;
import scratch.newast.Constants;
import scratch.newast.ParsingException;
import scratch.newast.model.statement.Stmt;
import scratch.newast.opcodes.BoolExprOpcode;
import scratch.newast.opcodes.CommonStmtOpcode;
import scratch.newast.opcodes.ControlStmtOpcode;
import scratch.newast.opcodes.EntityLookStmtOpcode;
import scratch.newast.opcodes.EntitySoundStmtOpcode;
import scratch.newast.opcodes.NumExprOpcode;
import scratch.newast.opcodes.SpriteLookStmtOpcode;
import scratch.newast.opcodes.SpriteMotionStmtOpcode;
import scratch.newast.opcodes.StringExprOpcode;
import scratch.newast.opcodes.TerminationStmtOpcode;

public class StmtParser {

    public static Stmt parse(String blockID, JsonNode blocks) throws ParsingException {
        Preconditions.checkNotNull(blockID);
        Preconditions.checkNotNull(blocks);
        Preconditions.checkState(blocks.has(blockID), "No block for id %s", blockID);

        JsonNode current = blocks.get(blockID);
        String opcode = current.get(Constants.OPCODE_KEY).asText();

        Stmt stmt;
        if (TerminationStmtOpcode.contains(opcode)) {
            stmt = TerminationStmtParser.parseTerminationStmt(current, blocks);
            return stmt;
        } else if (EntityLookStmtOpcode.contains(opcode)) {
            stmt = EntityLookStmtParser.parse(current, blocks);
            return stmt;
        } else if (ControlStmtOpcode.contains(opcode)) {
            stmt = ControlStmtParser.parse(current, blocks);
            return stmt;
        } else if (BoolExprOpcode.contains(opcode) || NumExprOpcode.contains(opcode) || StringExprOpcode
            .contains(opcode)) {
            stmt = ExpressionStmtParser.parse(current, blocks);
            return stmt;
        } else if (CommonStmtOpcode.contains(opcode)) {
            throw new RuntimeException("Not implemented");
        } else if (SpriteMotionStmtOpcode.contains(opcode)) {
            throw new RuntimeException("Not implemented");
        } else if (SpriteLookStmtOpcode.contains(opcode)) {
            throw new RuntimeException("Not implemented");
        } else if (EntitySoundStmtOpcode.contains(opcode)) {
            throw new RuntimeException("Not implemented");
            // Are these corner cases we have to deal with separately
//        } else if (ProceduralStmtOpcode.contains(opcode)) {
//            throw new RuntimeException("Not implemented");
//        } else if (ListStmtOpcode.contains(opcode)) {
//            throw new RuntimeException("Not implemented");
        }

        throw new RuntimeException("Not implemented");
    }
}
