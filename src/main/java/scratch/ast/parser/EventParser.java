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
package scratch.ast.parser;

import static scratch.ast.Constants.FIELDS_KEY;
import static scratch.ast.Constants.FIELD_VALUE;
import static scratch.ast.Constants.OPCODE_KEY;
import static scratch.ast.opcodes.EventOpcode.control_start_as_clone;
import static scratch.ast.opcodes.EventOpcode.event_whenbackdropswitchesto;
import static scratch.ast.opcodes.EventOpcode.event_whenbroadcastreceived;
import static scratch.ast.opcodes.EventOpcode.event_whenflagclicked;
import static scratch.ast.opcodes.EventOpcode.event_whengreaterthan;
import static scratch.ast.opcodes.EventOpcode.event_whenkeypressed;
import static scratch.ast.opcodes.EventOpcode.event_whenthisspriteclicked;

import com.fasterxml.jackson.databind.JsonNode;
import scratch.utils.Preconditions;
import scratch.ast.ParsingException;
import scratch.ast.model.Key;
import scratch.ast.model.Message;
import scratch.ast.model.event.BackdropSwitchTo;
import scratch.ast.model.event.Clicked;
import scratch.ast.model.event.Event;
import scratch.ast.model.event.GreenFlag;
import scratch.ast.model.event.KeyPressed;
import scratch.ast.model.event.ReceptionOfMessage;
import scratch.ast.model.event.StartedAsClone;
import scratch.ast.model.event.VariableAboveValue;
import scratch.ast.model.expression.num.NumExpr;
import scratch.ast.model.variable.Identifier;
import scratch.ast.model.variable.StrId;
import scratch.ast.opcodes.EventOpcode;

public class EventParser {

    public static String INPUTS = "WHENGREATERTHANMENU";
    public static String KEY_OPTION = "KEY_OPTION";
    public static String BCAST_OPTION = "BROADCAST_OPTION";
    public static String VARIABLE_MENU = "WHENGREATERTHANMENU";
    public static String BACKDROP = "BACKDROP";

    public static Event parse(String blockID, JsonNode allBlocks) throws ParsingException {
        Preconditions.checkNotNull(blockID);
        Preconditions.checkNotNull(allBlocks);

        JsonNode current = allBlocks.get(blockID);
        String opcodeString = current.get(OPCODE_KEY).asText();
        Preconditions
            .checkArgument(EventOpcode.contains(opcodeString), "Given blockID does not point to an event block.");


        EventOpcode opcode = EventOpcode.valueOf(opcodeString);
        if (opcode.equals(event_whenflagclicked)) {
            return new GreenFlag();

        } else if (opcode.equals(event_whenkeypressed)) {
            Key key = KeyParser.parse(current, allBlocks);
            return new KeyPressed(key);

        } else if (opcode.equals(event_whenthisspriteclicked)) {
            return new Clicked();

        } else if (opcode.equals(event_whenbroadcastreceived)) {
            JsonNode fields = current.get(FIELDS_KEY);
            String msgValue = fields.get(BCAST_OPTION).get(FIELD_VALUE).asText();
            Message msg = new Message(msgValue);
            return new ReceptionOfMessage(msg);

        } else if (opcode.equals(control_start_as_clone)) {
            return new StartedAsClone();

        } else if (opcode.equals(event_whengreaterthan)) {

            String variableValue = current.get(FIELDS_KEY).get(VARIABLE_MENU).get(0).asText();
            Identifier var = new StrId(variableValue);

            NumExpr fieldValue = NumExprParser.parseNumExpr(current, 0, allBlocks);

            return new VariableAboveValue(var, fieldValue);

        } else if (opcode.equals(event_whenbackdropswitchesto)) {
            JsonNode fields = current.get(FIELDS_KEY);
            JsonNode backdropArray = fields.get(BACKDROP);
            String backdropName = backdropArray.get(FIELD_VALUE).asText();
            Identifier id = new StrId(backdropName);
            return new BackdropSwitchTo(id);

        } else {
            throw new IllegalStateException("EventBlock with opcode " + opcode + " was not parsed");
        }
    }
}
