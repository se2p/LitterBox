/*
 * Copyright (C) 2019-2022 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.ast.parser.stmt.mblock;

import com.fasterxml.jackson.databind.JsonNode;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.option.RobotAxis;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.reset.ResetAxis;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.reset.ResetStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.reset.ResetTimer2;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.mblock.ResetStmtOpcode;
import de.uni_passau.fim.se2.litterbox.ast.parser.metadata.BlockMetadataParser;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.FIELDS_KEY;
import static de.uni_passau.fim.se2.litterbox.ast.Constants.OPCODE_KEY;

public class ResetStmtParser {

    private static final String AXIS_KEY = "AXIS";

    public static ResetStmt parse(String blockId, JsonNode current, JsonNode blocks) throws ParsingException {
        Preconditions.checkNotNull(blockId);
        Preconditions.checkNotNull(current);
        Preconditions.checkNotNull(blocks);

        String opcodeString = current.get(OPCODE_KEY).asText();
        Preconditions
                .checkArgument(ResetStmtOpcode.contains(opcodeString), "Given blockId does not point to an Reset block.");

        ResetStmtOpcode opcode = ResetStmtOpcode.getOpcode(opcodeString);
        BlockMetadata metadata = BlockMetadataParser.parse(blockId, current);
        switch (opcode) {
            case reset_angle:
                String axisName = current.get(FIELDS_KEY).get(AXIS_KEY).get(0).asText();
                RobotAxis axis = new RobotAxis(axisName);
                return new ResetAxis(axis, metadata);

            case reset_timer:
            case show_reset_time:
                return new ResetTimer2(metadata);

            default:
                throw new IllegalStateException("Reset Block with opcode " + opcode + " was not parsed");
        }
    }
}
