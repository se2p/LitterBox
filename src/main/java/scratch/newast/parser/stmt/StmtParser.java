package scratch.newast.parser.stmt;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Preconditions;
import scratch.newast.Constants;
import scratch.newast.ParsingException;
import scratch.newast.model.statement.Stmt;
import scratch.newast.opcodes.ControlStmtOpcode;
import scratch.newast.opcodes.EntityLookStmtOpcode;
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
        }

        throw new RuntimeException("Not implemented");
    }
}
