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
import de.uni_passau.fim.se2.litterbox.ast.model.Key;
import de.uni_passau.fim.se2.litterbox.ast.model.Message;
import de.uni_passau.fim.se2.litterbox.ast.model.event.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.event.*;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.option.PressedState;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.option.RobotButton;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.option.RobotDirection;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.new_parser.raw_ast.RawBlock;
import de.uni_passau.fim.se2.litterbox.ast.new_parser.raw_ast.RawBlockId;
import de.uni_passau.fim.se2.litterbox.ast.new_parser.raw_ast.RawInput;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.EventOpcode;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParserState;

class EventConverter {

    private static final String BCAST_OPTION = "BROADCAST_OPTION";
    private static final String BRIGHTNESS_KEY = "BRIGHTNESS";
    private static final String MENU_LIST_KEY = "MENU_LIST";
    private static final String PRESSED_KEY = "IS_PRESS";

    private EventConverter() {
        throw new IllegalCallerException("utility class constructor");
    }

    static Event convertEvent(
            final ProgramParserState state,
            final RawBlockId id,
            final RawBlock.RawRegularBlock event
    ) {
        final BlockMetadata metadata = RawBlockMetadataConverter.convertBlockMetadata(id, event);
        final EventOpcode opcode = EventOpcode.getOpcode(event.opcode());

        return switch (opcode) {
            case event_whenflagclicked -> new GreenFlag(metadata);
            case event_whenstageclicked -> new StageClicked(metadata);
            case event_whenthisspriteclicked -> new SpriteClicked(metadata);
            case control_start_as_clone -> new StartedAsClone(metadata);
            case when_board_launch, main -> new BoardLaunch(metadata);
            case when_board_shake -> new BoardShaken(metadata);
            case event_whenbroadcastreceived -> {
                final String message = event.fields().get(BCAST_OPTION).value().toString();
                final Message msg = new Message(new StringLiteral(message));
                yield new ReceptionOfMessage(msg, metadata);
            }
            case event_whenbackdropswitchesto -> {
                final String backdropName = event.fields().get(Constants.BACKDROP_INPUT).value().toString();
                final StrId backdropId = new StrId(backdropName);
                yield new BackdropSwitchTo(backdropId, metadata);
            }
            case event_whenkeypressed -> {
                final Key key = KeyConverter.convertKey(state, event);
                yield new KeyPressed(key, metadata);
            }
            case event_whengreaterthan -> {
                final String attributeName = event.fields().get(Constants.WHEN_GREATER_THAN_MENU).value().toString();
                final EventAttribute attr = new EventAttribute(attributeName.toLowerCase());
                final NumExpr value = NumExprConverter.convertNumExpr(
                        state, event, event.inputs().get(Constants.VALUE_KEY)
                );
                yield new AttributeAboveValue(attr, value, metadata);
            }
            case when_volume_over -> {
                final String attributeName = event.fields().get(MENU_LIST_KEY).value().toString();
                final EventAttribute attr = new EventAttribute(attributeName.toLowerCase());
                final NumExpr value = NumExprConverter.convertNumExpr(
                        state, event, event.inputs().get(Constants.VALUE_KEY)
                );
                yield new AttributeAboveValue(attr, value, metadata);
            }
            case when_button_press -> {
                final String buttonName = event.fields().get(Constants.BUTTONS_KEY).value().toString();
                final RobotButton button = new RobotButton(buttonName);
                yield new LaunchButton(button, metadata);
            }
            case when_board_button -> {
                final String pressedState = event.fields().get(PRESSED_KEY).value().toString();
                final PressedState pressed = new PressedState(pressedState);
                yield new BoardButtonAction(pressed, metadata);
            }
            case when_board_tilt -> {
                final String directionName = event.fields().get(Constants.DIRECTION_KEY_CAP).value().toString();
                final RobotDirection direction = new RobotDirection(directionName);
                yield new BoardTilted(direction, metadata);
            }
            case when_brightness_less -> {
                final RawInput numExprInput = event.inputs().get(BRIGHTNESS_KEY);
                final NumExpr fieldValue = NumExprConverter.convertNumExpr(state, event, numExprInput);
                yield new BrightnessLess(fieldValue, metadata);
            }
        };
    }
}
