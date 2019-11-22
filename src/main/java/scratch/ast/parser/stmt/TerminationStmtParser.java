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
package scratch.ast.parser.stmt;

import com.fasterxml.jackson.databind.JsonNode;
import scratch.utils.Preconditions;
import scratch.ast.Constants;
import scratch.ast.ParsingException;
import scratch.ast.model.statement.termination.DeleteClone;
import scratch.ast.model.statement.termination.StopAll;
import scratch.ast.model.statement.termination.StopThisScript;
import scratch.ast.model.statement.termination.TerminationStmt;
import scratch.ast.opcodes.TerminationStmtOpcode;

public class TerminationStmtParser {

    private static final String STOP_OPTION = "STOP_OPTION";
    private static final String STOP_ALL = "all";
    private static final String STOP_THIS = "this script";

    public static TerminationStmt parseTerminationStmt(JsonNode current, JsonNode blocks)
        throws ParsingException {
        Preconditions.checkNotNull(current);
        Preconditions.checkNotNull(blocks);

        final String opCodeString = current.get(Constants.OPCODE_KEY).asText();
        if (!TerminationStmtOpcode.contains(opCodeString)) {
            throw new ParsingException(
                "Called parseTerminationStmt with a block that does not qualify as such"
                    + " a statement. Opcode is " + opCodeString);
        }

        final TerminationStmtOpcode opcode = TerminationStmtOpcode.valueOf(opCodeString);
        switch (opcode) {
            case control_stop:
                return parseControlStop(current);
            case control_delete_this_clone:
                return new DeleteClone();
            default:
                throw new RuntimeException("Not implemented yet for opcode " + opcode);
        }
    }

    private static TerminationStmt parseControlStop(JsonNode current) throws ParsingException {
        final String stopOptionValue = current
                .get(Constants.FIELDS_KEY)
                .get(STOP_OPTION)
                .get(Constants.FIELD_VALUE)
                .asText();

        if (stopOptionValue.equals(STOP_ALL)) {
            return new StopAll();

        } else if (stopOptionValue.equals(STOP_THIS)) {
            return new StopThisScript();

        } else {
            throw new ParsingException(
                "Unknown Stop Option Value "
                    + stopOptionValue
                    + " for TerminationStmt "
                    + TerminationStmtOpcode.control_stop.name());
        }
    }
}
