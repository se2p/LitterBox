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
import de.uni_passau.fim.se2.litterbox.ast.Constants;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.DeleteClone;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopAll;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopThisScript;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.TerminationStmt;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.TerminationStmtOpcode;
import de.uni_passau.fim.se2.litterbox.ast.parser.metadata.BlockMetadataParser;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

public class TerminationStmtParser {

    private static final String STOP_OPTION = "STOP_OPTION";
    private static final String STOP_ALL = "all";
    private static final String STOP_THIS = "this script";

    public static TerminationStmt parseTerminationStmt(String identifier, JsonNode current, JsonNode blocks)
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
        BlockMetadata metadata = BlockMetadataParser.parse(identifier, current);
        switch (opcode) {
            case control_stop:
                return parseControlStop(current, metadata);
            case control_delete_this_clone:
                return new DeleteClone(metadata);
            default:
                throw new RuntimeException("Not implemented yet for opcode " + opcode);
        }
    }

    private static TerminationStmt parseControlStop(JsonNode current, BlockMetadata metadata) throws ParsingException {
        final String stopOptionValue = current
                .get(Constants.FIELDS_KEY)
                .get(STOP_OPTION)
                .get(Constants.FIELD_VALUE)
                .asText();

        if (stopOptionValue.equals(STOP_ALL)) {
            return new StopAll(metadata);
        } else if (stopOptionValue.equals(STOP_THIS)) {
            return new StopThisScript(metadata);
        } else {
            throw new ParsingException(
                    "Unknown Stop Option Value "
                            + stopOptionValue
                            + " for TerminationStmt "
                            + TerminationStmtOpcode.control_stop.name());
        }
    }
}
