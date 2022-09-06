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
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.StringExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.option.LEDMatrix;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.option.MCorePort;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.ledmatrix.*;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.mblock.LEDMatrixStmtOpcode;
import de.uni_passau.fim.se2.litterbox.ast.parser.NumExprParser;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParserState;
import de.uni_passau.fim.se2.litterbox.ast.parser.StringExprParser;
import de.uni_passau.fim.se2.litterbox.ast.parser.metadata.BlockMetadataParser;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;

public class LEDMatrixStmtParser {

    private static final String PANEL_KEY = "PANEL";
    private static final String AXIS_X_KEY = "AXIS-X";
    private static final String AXIS_Y_KEY = "AXIS-Y";
    private static final String X_AXIS_KEY = "X";
    private static final String Y_AXIS_KEY = "Y";
    private static final String FACE_PANEL_KEY = "FACE_PANEL";

    public static LEDMatrixStmt parse(final ProgramParserState state, String blockId, JsonNode current, JsonNode blocks) throws ParsingException {
        Preconditions.checkNotNull(blockId);
        Preconditions.checkNotNull(current);
        Preconditions.checkNotNull(blocks);

        String opcodeString = current.get(OPCODE_KEY).asText();
        Preconditions
                .checkArgument(LEDMatrixStmtOpcode.contains(opcodeString), "Given blockId does not point to an LED Matrix block.");

        LEDMatrixStmtOpcode opcode = LEDMatrixStmtOpcode.getOpcode(opcodeString);
        BlockMetadata metadata = BlockMetadataParser.parse(blockId, current);
        switch (opcode) {
            case show_led_matrix_face_with_time:
                String panel = current.get(FIELDS_KEY).get(PANEL_KEY).get(0).asText();
                LEDMatrix matrix = new LEDMatrix(panel);
                NumExpr time = NumExprParser.parseNumExpr(state, current, TIME_KEY, blocks);
                return new FaceTimed(matrix, time, metadata);

            case show_led_matrix_face:
                panel = current.get(FIELDS_KEY).get(PANEL_KEY).get(0).asText();
                matrix = new LEDMatrix(panel);
                return new ShowFace(matrix, metadata);

            case show_led_matrix_face_position:
                panel = current.get(FIELDS_KEY).get(PANEL_KEY).get(0).asText();
                matrix = new LEDMatrix(panel);
                NumExpr xAxis = NumExprParser.parseNumExpr(state, current, AXIS_X_KEY, blocks);
                NumExpr yAxis = NumExprParser.parseNumExpr(state, current, AXIS_Y_KEY, blocks);
                return new FacePosition(matrix, xAxis, yAxis, metadata);

            case show_led_matrix_turn_off:
                return new TurnOffFace(metadata);

            case show_led_matrix:
                StringExpr text = StringExprParser.parseStringExpr(state, current, STRING_KEY, blocks);
                return new LEDString(text, metadata);

            case show_led_matrix_string:
                text = StringExprParser.parseStringExpr(state, current, STRING_KEY, blocks);
                return new LEDStringScrolling(text, metadata);

            case show_led_matrix_string_position:
                text = StringExprParser.parseStringExpr(state, current, STRING_KEY, blocks);
                xAxis = NumExprParser.parseNumExpr(state, current, X_AXIS_KEY, blocks);
                yAxis = NumExprParser.parseNumExpr(state, current, Y_AXIS_KEY, blocks);
                return new LEDStringPosition(text, xAxis, yAxis, metadata);

            case show_led_matrix_position:
                xAxis = NumExprParser.parseNumExpr(state, current, X_AXIS_KEY, blocks);
                yAxis = NumExprParser.parseNumExpr(state, current, Y_AXIS_KEY, blocks);
                return new LEDSwitchOn(xAxis, yAxis, metadata);

            case light_off_led_matrix_position:
                xAxis = NumExprParser.parseNumExpr(state, current, X_AXIS_KEY, blocks);
                yAxis = NumExprParser.parseNumExpr(state, current, Y_AXIS_KEY, blocks);
                return new LEDSwitchOff(xAxis, yAxis, metadata);

            case toggle_led_matrix_position:
                xAxis = NumExprParser.parseNumExpr(state, current, X_AXIS_KEY, blocks);
                yAxis = NumExprParser.parseNumExpr(state, current, Y_AXIS_KEY, blocks);
                return new LEDToggle(xAxis, yAxis, metadata);

            case show_face_time:
                String portNumber = current.get(FIELDS_KEY).get(PORT_KEY).get(0).asText();
                MCorePort port = new MCorePort(portNumber);
                panel = current.get(FIELDS_KEY).get(FACE_PANEL_KEY).get(0).asText();
                matrix = new LEDMatrix(panel);
                time = NumExprParser.parseNumExpr(state, current, TIME_KEY, blocks);
                return new FaceTimedPort(port, matrix, time, metadata);

            case show_face:
                portNumber = current.get(FIELDS_KEY).get(PORT_KEY).get(0).asText();
                port = new MCorePort(portNumber);
                panel = current.get(FIELDS_KEY).get(FACE_PANEL_KEY).get(0).asText();
                matrix = new LEDMatrix(panel);
                return new ShowFacePort(port, matrix, metadata);

            case show_face_position:
                portNumber = current.get(FIELDS_KEY).get(PORT_KEY).get(0).asText();
                port = new MCorePort(portNumber);
                panel = current.get(FIELDS_KEY).get(FACE_PANEL_KEY).get(0).asText();
                matrix = new LEDMatrix(panel);
                xAxis = NumExprParser.parseNumExpr(state, current, X_AXIS_KEY, blocks);
                yAxis = NumExprParser.parseNumExpr(state, current, Y_AXIS_KEY, blocks);
                return new FacePositionPort(port, matrix, xAxis, yAxis, metadata);

            case show_text:
                portNumber = current.get(FIELDS_KEY).get(PORT_KEY).get(0).asText();
                port = new MCorePort(portNumber);
                text = StringExprParser.parseStringExpr(state, current, TEXT_KEY_CAPS, blocks);
                return new LEDStringPort(port, text, metadata);

            case show_text_position:
                portNumber = current.get(FIELDS_KEY).get(PORT_KEY).get(0).asText();
                port = new MCorePort(portNumber);
                text = StringExprParser.parseStringExpr(state, current, TEXT_KEY_CAPS, blocks);
                xAxis = NumExprParser.parseNumExpr(state, current, X_AXIS_KEY, blocks);
                yAxis = NumExprParser.parseNumExpr(state, current, Y_AXIS_KEY, blocks);
                return new LEDStringPositionPort(port, text, xAxis, yAxis, metadata);

            case show_number:
                portNumber = current.get(FIELDS_KEY).get(PORT_KEY).get(0).asText();
                port = new MCorePort(portNumber);
                NumExpr number = NumExprParser.parseNumExpr(state, current, NUMBER_KEY, blocks);
                return new LEDNumPort(port, number, metadata);

            case show_time:
                portNumber = current.get(FIELDS_KEY).get(PORT_KEY).get(0).asText();
                port = new MCorePort(portNumber);
                NumExpr hour = NumExprParser.parseNumExpr(state, current, NUMBER1_KEY, blocks);
                NumExpr minute = NumExprParser.parseNumExpr(state, current, NUMBER2_KEY, blocks);
                return new LEDTimePort(port, hour, minute, metadata);

            case show_face_off:
                portNumber = current.get(FIELDS_KEY).get(PORT_KEY).get(0).asText();
                port = new MCorePort(portNumber);
                return new TurnOffFacePort(port, metadata);

            default:
                throw new IllegalStateException("LEDMatrixStmt Block with opcode " + opcode + " was not parsed");
        }
    }
}
