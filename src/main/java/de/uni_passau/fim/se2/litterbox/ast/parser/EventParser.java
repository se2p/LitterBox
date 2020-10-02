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

import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;
import static de.uni_passau.fim.se2.litterbox.ast.opcodes.EventOpcode.*;

public class EventParser {

    public static final String INPUTS = "WHENGREATERTHANMENU";
    public static final String KEY_OPTION = "KEY_OPTION";
    public static final String BCAST_OPTION = "BROADCAST_OPTION";
    public static final String GREATER_THAN_MENU = "WHENGREATERTHANMENU";
    public static final String BACKDROP = "BACKDROP";

    public static Event parse(String blockId, JsonNode allBlocks) throws ParsingException {
        Preconditions.checkNotNull(blockId);
        Preconditions.checkNotNull(allBlocks);

        JsonNode current = allBlocks.get(blockId);
        String opcodeString = current.get(OPCODE_KEY).asText();
        Preconditions
                .checkArgument(EventOpcode.contains(opcodeString), "Given blockId does not point to an event block.");

        EventOpcode opcode = EventOpcode.valueOf(opcodeString);
        BlockMetadata metadata = BlockMetadataParser.parse(blockId, current);
        if (opcode.equals(event_whenflagclicked)) {
            return new GreenFlag(metadata);
        } else if (opcode.equals(event_whenkeypressed)) {
            Key key = KeyParser.parse(current, allBlocks);
            return new KeyPressed(key, metadata);
        } else if (opcode.equals(event_whenthisspriteclicked)) {
            return new SpriteClicked(metadata);
        } else if (opcode.equals(event_whenstageclicked)) {
            return new StageClicked(metadata);
        } else if (opcode.equals(event_whenbroadcastreceived)) {
            JsonNode fields = current.get(FIELDS_KEY);
            String msgValue = fields.get(BCAST_OPTION).get(FIELD_VALUE).asText();
            Message msg = new Message(new StringLiteral(msgValue));
            return new ReceptionOfMessage(msg, metadata);
        } else if (opcode.equals(control_start_as_clone)) {
            return new StartedAsClone(metadata);
        } else if (opcode.equals(event_whengreaterthan)) {
            String variableValue = current.get(FIELDS_KEY).get(GREATER_THAN_MENU).get(0).asText();
            EventAttribute attr = new EventAttribute(variableValue.toLowerCase());
            NumExpr fieldValue = NumExprParser.parseNumExpr(current, VALUE_KEY, allBlocks);
            return new AttributeAboveValue(attr, fieldValue, metadata);
        } else if (opcode.equals(event_whenbackdropswitchesto)) {
            JsonNode fields = current.get(FIELDS_KEY);
            JsonNode backdropArray = fields.get(BACKDROP);
            String backdropName = backdropArray.get(FIELD_VALUE).asText();
            LocalIdentifier id = new StrId(backdropName);
            return new BackdropSwitchTo(id, metadata);
        } else {
            throw new IllegalStateException("EventBlock with opcode " + opcode + " was not parsed");
        }
    }
}
