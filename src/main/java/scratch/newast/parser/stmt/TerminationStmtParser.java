package scratch.newast.parser.stmt;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Preconditions;
import scratch.newast.Constants;
import scratch.newast.ParsingException;
import scratch.newast.model.statement.termination.DeleteClone;
import scratch.newast.model.statement.termination.StopAll;
import scratch.newast.model.statement.termination.StopThisScript;
import scratch.newast.model.statement.termination.TerminationStmt;
import scratch.newast.opcodes.TerminationStmtOpcode;

public class TerminationStmtParser {

    private static final String STOP_OPTION = "STOP_OPTION";
    private static final String STOP_ALL = "all";
    private static final String STOP_THIS = "this script";

    public static TerminationStmt parseTerminationStmt(JsonNode current, JsonNode blocks)
        throws ParsingException {
        Preconditions.checkNotNull(current);
        Preconditions.checkNotNull(blocks);

        String opCodeString = current.get(Constants.OPCODE_KEY).asText();
        if (!TerminationStmtOpcode.contains(opCodeString)) {
            throw new ParsingException(
                "Called parseTerminationStmt with a block that does not qualify as such"
                    + " a statement. Opcode is " + opCodeString);
        }

        TerminationStmtOpcode opcode = TerminationStmtOpcode.valueOf(opCodeString);
        TerminationStmt stmt;
        switch (opcode) {
            case control_stop:
                stmt = parseControlStop(current);
                break;
            case control_delete_this_clone:
                stmt = new DeleteClone();
                break;
            default:
                throw new RuntimeException("Not implemented yet for opcode " + opcode);
        }

        return stmt;
    }

    private static TerminationStmt parseControlStop(JsonNode current) throws ParsingException {
        TerminationStmt stmt;
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
        return stmt;
    }
}
