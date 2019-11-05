package scratch.newast.parser;

import com.fasterxml.jackson.databind.JsonNode;
import scratch.newast.Constants;
import scratch.newast.model.statement.Stmt;
import scratch.newast.opcodes.TerminationStmtOpcode;

public class StmtParser {


    public static Stmt parse(JsonNode current, JsonNode blocks) {
        String opcode = current.get(Constants.OPCODE_KEY).asText();

        if (TerminationStmtOpcode.contains(opcode)) {
            throw new RuntimeException("TerminationStmt parsing not implemented yet");
        } else {

        }

        throw new RuntimeException("Not implemented");
    }
}
