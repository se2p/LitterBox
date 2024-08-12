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
package de.uni_passau.fim.se2.litterbox.ast.parser;

import com.fasterxml.jackson.databind.JsonNode;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Key;
import de.uni_passau.fim.se2.litterbox.ast.model.Message;
import de.uni_passau.fim.se2.litterbox.ast.model.event.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.LocalIdentifier;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.EventOpcode;
import de.uni_passau.fim.se2.litterbox.ast.parser.metadata.BlockMetadataParser;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.event.*;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.option.PressedState;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.option.RobotButton;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.option.RobotDirection;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;

public class EventParser {

    public static final String INPUTS = "WHENGREATERTHANMENU";
    public static final String KEY_OPTION = "KEY_OPTION";
    public static final String BCAST_OPTION = "BROADCAST_OPTION";
    public static final String GREATER_THAN_MENU = "WHENGREATERTHANMENU";
    public static final String BACKDROP = "BACKDROP";

    public static final String BRIGHTNESS_KEY = "BRIGHTNESS";
    public static final String PRESSED_KEY = "IS_PRESS";
    public static final String MENU_LIST_KEY = "MENU_LIST";
    public static final String SOUND_VOLUME_KEY = "SOUNDVOLUME";

    public static Event parse(final ProgramParserState state, String blockId, JsonNode allBlocks)
            throws ParsingException {
        Preconditions.checkNotNull(blockId);
        Preconditions.checkNotNull(allBlocks);

        JsonNode current = allBlocks.get(blockId);
        String opcodeString = current.get(OPCODE_KEY).asText();
        Preconditions
                .checkArgument(EventOpcode.contains(opcodeString), "Given blockId does not point to an event block.");

        EventOpcode opcode = EventOpcode.getOpcode(opcodeString);
        BlockMetadata metadata = BlockMetadataParser.parse(blockId, current);
        switch (opcode) {
            case event_whenflagclicked:
                return new GreenFlag(metadata);

            case event_whenkeypressed:
                Key key = KeyParser.parse(state, current, allBlocks);
                return new KeyPressed(key, metadata);

            case event_whenstageclicked:
                return new StageClicked(metadata);

            case event_whenthisspriteclicked:
                return new SpriteClicked(metadata);

            case event_whenbackdropswitchesto:
                JsonNode fields = current.get(FIELDS_KEY);
                JsonNode backdropArray = fields.get(BACKDROP);
                String backdropName = backdropArray.get(FIELD_VALUE).asText();
                LocalIdentifier id = new StrId(backdropName);
                return new BackdropSwitchTo(id, metadata);

            case event_whengreaterthan:
                String variableValue = current.get(FIELDS_KEY).get(GREATER_THAN_MENU).get(0).asText();
                EventAttribute attr = new EventAttribute(variableValue.toLowerCase());
                NumExpr fieldValue = NumExprParser.parseNumExpr(state, current, VALUE_KEY, allBlocks);
                return new AttributeAboveValue(attr, fieldValue, metadata);

            case event_whenbroadcastreceived:
                fields = current.get(FIELDS_KEY);
                String msgValue = fields.get(BCAST_OPTION).get(FIELD_VALUE).asText();
                Message msg = new Message(new StringLiteral(msgValue));
                return new ReceptionOfMessage(msg, metadata);

            case control_start_as_clone:
                return new StartedAsClone(metadata);

            // mBlock Events
            case when_board_launch:
            case main:
                return new BoardLaunch(metadata);

            case when_button_press:
                String buttonName = current.get(FIELDS_KEY).get(BUTTONS_KEY).get(0).asText();
                RobotButton button = new RobotButton(buttonName);
                return new LaunchButton(button, metadata);

            case when_board_button:
                // not same as when_button_press
                String pressedState = current.get(FIELDS_KEY).get(PRESSED_KEY).get(0).asText();
                PressedState pressed = new PressedState(pressedState);
                return new BoardButtonAction(pressed, metadata);

            case when_board_shake:
                return new BoardShaken(metadata);

            case when_board_tilt:
                String directionName = current.get(FIELDS_KEY).get(DIRECTION_KEY_CAP).get(0).asText();
                RobotDirection direction = new RobotDirection(directionName);
                return new BoardTilted(direction, metadata);

            case when_volume_over:
                // equal but not the same as event_whengreaterthan
                variableValue = current.get(FIELDS_KEY).get(MENU_LIST_KEY).get(0).asText();
                attr = new EventAttribute(variableValue.toLowerCase());
                fieldValue = NumExprParser.parseNumExpr(state, current, SOUND_VOLUME_KEY, allBlocks);
                return new AttributeAboveValue(attr, fieldValue, metadata);

            case when_brightness_less:
                fieldValue = NumExprParser.parseNumExpr(state, current, BRIGHTNESS_KEY, allBlocks);
                return new BrightnessLess(fieldValue, metadata);

            default:
                throw new IllegalStateException("EventBlock with opcode " + opcode + " was not parsed");
        }
    }
}
