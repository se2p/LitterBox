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

import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.StringExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.option.LEDColor;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.option.LEDPosition;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.option.RGB;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.led.*;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.new_parser.raw_ast.KnownFields;
import de.uni_passau.fim.se2.litterbox.ast.new_parser.raw_ast.KnownInputs;
import de.uni_passau.fim.se2.litterbox.ast.new_parser.raw_ast.RawBlock;
import de.uni_passau.fim.se2.litterbox.ast.new_parser.raw_ast.RawBlockId;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.mblock.LEDStmtOpcode;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParserState;

final class LedStmtConverter extends StmtConverter<LEDStmt> {

    LedStmtConverter(ProgramParserState state) {
        super(state);
    }

    @Override
    LEDStmt convertStmt(final RawBlockId blockId, final RawBlock.RawRegularBlock block) {
        final LEDStmtOpcode opcode = LEDStmtOpcode.getOpcode(block.opcode());
        final BlockMetadata metadata = RawBlockMetadataConverter.convertBlockMetadata(blockId, block);

        return switch (opcode) {
            case show_led_with_time -> {
                final StringExpr color = getColor(block);
                final NumExpr time = getTime(block);
                yield new LEDColorTimed(color, time, metadata);
            }
            case show_led -> {
                final StringExpr color = getColor(block);

                if (block.opcode().contains("mcore.")) {
                    final LEDPosition ledPosition = getLedPosition(block);
                    yield new LEDColorShowPosition(ledPosition, color, metadata);
                } else {
                    yield new LEDColorShow(color, metadata);
                }
            }
            case show_led_rgb -> {
                if (block.opcode().contains("mcore.")) {
                    final LEDPosition ledPosition = getLedPosition(block);
                    final NumExpr red = NumExprConverter.convertNumExpr(state, block, KnownInputs.LED_RED);
                    final NumExpr green = NumExprConverter.convertNumExpr(state, block, KnownInputs.LED_GREEN);
                    final NumExpr blue = NumExprConverter.convertNumExpr(state, block, KnownInputs.LED_BLUE);

                    yield new RGBValuesPosition(ledPosition, red, green, blue, metadata);
                } else {
                    final String rgbName = block.getFieldValueAsString(KnownFields.RGB);
                    final RGB rgb = new RGB(rgbName);
                    final NumExpr value = NumExprConverter.convertNumExpr(state, block, KnownInputs.VALUE);

                    yield new RGBValue(rgb, value, metadata);
                }
            }
            case turn_off_led -> new LEDOff(metadata);
            case rocky_show_led_color -> {
                final String colorName = block.getFieldValueAsString(KnownFields.COLORLIST);
                final LEDColor color = new LEDColor(colorName);
                yield new RockyLight(color, metadata);
            }
            case rocky_turn_off_led_color -> new RockyLightOff(metadata);
            case show_led_time -> {
                final LEDPosition ledPosition = getLedPosition(block);
                final StringExpr color = getColor(block);
                final NumExpr time = getTime(block);
                yield new LEDColorTimedPosition(ledPosition, color, time, metadata);
            }
        };
    }

    private LEDPosition getLedPosition(final RawBlock.RawRegularBlock block) {
        final String positionName = block.getFieldValueAsString(KnownFields.LED_POSITION);
        return new LEDPosition(positionName);
    }

    private StringExpr getColor(final RawBlock.RawRegularBlock block) {
        return StringExprConverter.convertStringExpr(state, block, KnownInputs.COLOR);
    }

    private NumExpr getTime(final RawBlock.RawRegularBlock block) {
        return NumExprConverter.convertNumExpr(state, block, KnownInputs.TIME);
    }
}
