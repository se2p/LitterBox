/*
 * Copyright (C) 2019-2024 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.ast.new_parser;

import de.uni_passau.fim.se2.litterbox.ast.Constants;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.StringExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.option.LEDMatrix;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.option.MCorePort;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.ledmatrix.*;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.new_parser.raw_ast.RawBlock;
import de.uni_passau.fim.se2.litterbox.ast.new_parser.raw_ast.RawBlockId;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.mblock.LEDMatrixStmtOpcode;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParserState;

final class LedMatrixStmtConverter extends StmtConverter<LEDMatrixStmt> {

    private static final String PANEL_KEY = "PANEL";
    private static final String AXIS_X_KEY = "AXIS-X";
    private static final String AXIS_Y_KEY = "AXIS-Y";
    private static final String X_AXIS_KEY = "X";
    private static final String Y_AXIS_KEY = "Y";
    private static final String FACE_PANEL_KEY = "FACE_PANEL";

    LedMatrixStmtConverter(ProgramParserState state) {
        super(state);
    }

    @Override
    LEDMatrixStmt convertStmt(final RawBlockId blockId, final RawBlock.RawRegularBlock block) {
        final LEDMatrixStmtOpcode opcode = LEDMatrixStmtOpcode.getOpcode(block.opcode());
        final BlockMetadata metadata = RawBlockMetadataConverter.convertBlockMetadata(blockId, block);

        return switch (opcode) {
            case show_led_matrix_face_with_time -> {
                final LEDMatrix matrix = getPanel(block);
                final NumExpr time = NumExprConverter.convertNumExpr(
                        state, block, block.inputs().get(Constants.TIME_KEY)
                );
                yield new FaceTimed(matrix, time, metadata);
            }
            case show_led_matrix_face -> {
                final LEDMatrix matrix = getPanel(block);
                yield new ShowFace(matrix, metadata);
            }
            case show_led_matrix_face_position -> {
                final LEDMatrix matrix = getPanel(block);
                // note the different keys here AXIS_X_KEY instead of X_AXIS_KEY
                final NumExpr x = NumExprConverter.convertNumExpr(state, block, block.inputs().get(AXIS_X_KEY));
                final NumExpr y = NumExprConverter.convertNumExpr(state, block, block.inputs().get(AXIS_Y_KEY));
                yield new FacePosition(matrix, x, y, metadata);
            }
            case show_led_matrix_turn_off -> new TurnOffFace(metadata);
            case show_led_matrix -> {
                final StringExpr text = StringExprConverter.convertStringExpr(
                        state, block, block.inputs().get(Constants.STRING_KEY)
                );
                yield new LEDString(text, metadata);
            }
            case show_led_matrix_string -> {
                final StringExpr text = StringExprConverter.convertStringExpr(
                        state, block, block.inputs().get(Constants.STRING_KEY)
                );
                yield new LEDStringScrolling(text, metadata);
            }
            case show_led_matrix_string_position -> {
                final StringExpr text = StringExprConverter.convertStringExpr(
                        state, block, block.inputs().get(Constants.STRING_KEY)
                );
                final NumExpr x = getX(block);
                final NumExpr y = getY(block);

                yield new LEDStringPosition(text, x, y, metadata);
            }
            case show_led_matrix_position -> {
                final NumExpr x = getX(block);
                final NumExpr y = getY(block);
                yield new LEDSwitchOn(x, y, metadata);
            }
            case light_off_led_matrix_position -> {
                final NumExpr x = getX(block);
                final NumExpr y = getY(block);
                yield new LEDSwitchOff(x, y, metadata);
            }
            case toggle_led_matrix_position -> {
                final NumExpr x = getX(block);
                final NumExpr y = getY(block);
                yield new LEDToggle(x, y, metadata);
            }
            case show_face_time -> {
                final MCorePort port = getPort(block);
                final LEDMatrix matrix = getFacePanel(block);
                final NumExpr time = NumExprConverter.convertNumExpr(
                        state, block, block.inputs().get(Constants.TIME_KEY)
                );

                yield new FaceTimedPort(port, matrix, time, metadata);
            }
            case show_face -> {
                final MCorePort port = getPort(block);
                final LEDMatrix matrix = getFacePanel(block);
                yield new ShowFacePort(port, matrix, metadata);
            }
            case show_face_position -> {
                final MCorePort port = getPort(block);
                final LEDMatrix matrix = getFacePanel(block);
                final NumExpr x = getX(block);
                final NumExpr y = getY(block);

                yield new FacePositionPort(port, matrix, x, y, metadata);
            }
            case show_text -> {
                final MCorePort port = getPort(block);
                final StringExpr text = StringExprConverter.convertStringExpr(
                        state, block, block.inputs().get(Constants.TEXT_KEY_CAPS)
                );
                yield new LEDStringPort(port, text, metadata);
            }
            case show_text_position -> {
                final MCorePort port = getPort(block);
                final StringExpr text = StringExprConverter.convertStringExpr(
                        state, block, block.inputs().get(Constants.TEXT_KEY_CAPS)
                );
                final NumExpr x = getX(block);
                final NumExpr y = getY(block);

                yield new LEDStringPositionPort(port, text, x, y, metadata);
            }
            case show_number -> {
                final MCorePort port = getPort(block);
                final NumExpr number = NumExprConverter.convertNumExpr(
                        state, block, block.inputs().get(Constants.NUMBER_KEY)
                );
                yield new LEDNumPort(port, number, metadata);
            }
            case show_time -> {
                final MCorePort port = getPort(block);
                final NumExpr hour = NumExprConverter.convertNumExpr(
                        state, block, block.inputs().get(Constants.NUMBER1_KEY)
                );
                final NumExpr minute = NumExprConverter.convertNumExpr(
                        state, block, block.inputs().get(Constants.NUMBER2_KEY)
                );
                yield new LEDTimePort(port, hour, minute, metadata);
            }
            case show_face_off -> {
                final MCorePort port = getPort(block);
                yield new TurnOffFacePort(port, metadata);
            }
        };
    }

    private LEDMatrix getPanel(final RawBlock.RawRegularBlock block) {
        final String panel = block.fields().get(PANEL_KEY).value().toString();
        return new LEDMatrix(panel);
    }

    private LEDMatrix getFacePanel(final RawBlock.RawRegularBlock block) {
        final String panel = block.fields().get(FACE_PANEL_KEY).value().toString();
        return new LEDMatrix(panel);
    }

    private MCorePort getPort(final RawBlock.RawRegularBlock block) {
        final String portId = block.fields().get(Constants.PORT_KEY).value().toString();
        return new MCorePort(portId);
    }

    private NumExpr getX(final RawBlock.RawRegularBlock block) {
        return NumExprConverter.convertNumExpr(state, block, block.inputs().get(X_AXIS_KEY));
    }

    private NumExpr getY(final RawBlock.RawRegularBlock block) {
        return NumExprConverter.convertNumExpr(state, block, block.inputs().get(Y_AXIS_KEY));
    }
}
