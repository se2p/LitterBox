package scratch.newast.parser.stmt;

import com.fasterxml.jackson.databind.JsonNode;
import scratch.newast.Constants;
import scratch.newast.ParsingException;
import scratch.newast.model.statement.Stmt;
import scratch.newast.model.statement.termination.DeleteClone;
import scratch.newast.model.statement.termination.StopAll;
import scratch.newast.model.statement.termination.StopThisScript;
import scratch.newast.model.statement.termination.TerminationStmt;
import scratch.newast.opcodes.EntityLookStmtOpcode;
import scratch.newast.opcodes.TerminationStmtOpcode;

public class StmtParser {

    private static final String STOP_OPTION = "STOP_OPTION";
    private static final String STOP_ALL = "all";
    private static final String STOP_THIS = "this script";

    public static Stmt parse(JsonNode current, JsonNode blocks) throws ParsingException {
        String opcode = current.get(Constants.OPCODE_KEY).asText();

        Stmt stmt;
        if (TerminationStmtOpcode.contains(opcode)) {
            TerminationStmt terminationStmt = parseTerminationStmt(current, blocks);
            stmt = terminationStmt;
            return stmt;
        } else if (EntityLookStmtOpcode.contains(opcode)) {
            stmt = EntityLookStmtParser.parse(current, blocks);
            return stmt;
        }

        throw new RuntimeException("Not implemented");
    }

    private static TerminationStmt parseTerminationStmt(JsonNode current, JsonNode blocks)
        throws ParsingException {
        String opCodeString = current.get(Constants.OPCODE_KEY).asText();
        if (TerminationStmtOpcode.contains(opCodeString)) {
            throw new ParsingException(
                "Called parseTerminationStmt with a block that does not qualify as such"
                    + "a statement");
        }

        TerminationStmtOpcode opcode = TerminationStmtOpcode.valueOf(opCodeString);
        TerminationStmt stmt;
        if (opcode.equals(TerminationStmtOpcode.control_delete_this_clone)) {
            stmt = new DeleteClone();
        } else if (opcode.equals(TerminationStmtOpcode.control_stop)) {
            String stopOptionValue =
                current.get(Constants.FIELDS_KEY).get(STOP_OPTION).get(Constants.FIELD_VALUE)
                    .asText();
            if (stopOptionValue.equals(STOP_ALL)) {
                stmt = new StopAll();
            } else if (stopOptionValue.equals(STOP_THIS)) {
                stmt = new StopThisScript();
            } else {
                throw new ParsingException(
                    "Unknown Stop Option Value "
                        + stopOptionValue
                        + " for TerminationStmt "
                        + TerminationStmtOpcode.control_stop.name());
            }
        } else {
            throw new RuntimeException("Not implemented yet for opcode " + opcode);
        }

        return stmt;
    }
}
