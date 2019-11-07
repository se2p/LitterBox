package scratch.newast.parser;

import static scratch.newast.Constants.FIELDS_KEY;
import static scratch.newast.Constants.FIELD_VALUE;
import static scratch.newast.Constants.OPCODE_KEY;
import static scratch.newast.opcodes.EventOpcode.control_start_as_clone;
import static scratch.newast.opcodes.EventOpcode.event_whenbackdropswitchesto;
import static scratch.newast.opcodes.EventOpcode.event_whenbroadcastreceived;
import static scratch.newast.opcodes.EventOpcode.event_whenflagclicked;
import static scratch.newast.opcodes.EventOpcode.event_whengreaterthan;
import static scratch.newast.opcodes.EventOpcode.event_whenkeypressed;
import static scratch.newast.opcodes.EventOpcode.event_whenthisspriteclicked;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Preconditions;
import scratch.newast.Constants;
import scratch.newast.ParsingException;
import scratch.newast.model.Key;
import scratch.newast.model.Message;
import scratch.newast.model.event.BackdropSwitchTo;
import scratch.newast.model.event.Clicked;
import scratch.newast.model.event.Event;
import scratch.newast.model.event.GreenFlag;
import scratch.newast.model.event.KeyPressed;
import scratch.newast.model.event.ReceptionOfMessage;
import scratch.newast.model.event.StartedAsClone;
import scratch.newast.model.event.VariableAboveValue;
import scratch.newast.model.expression.num.NumExpr;
import scratch.newast.model.variable.Identifier;
import scratch.newast.opcodes.EventOpcode;

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

        Event event;
        EventOpcode opcode = EventOpcode.valueOf(opcodeString);
        if (opcode.equals(event_whenflagclicked)) {
            event = new GreenFlag();
        } else if (opcode.equals(event_whenkeypressed)) {
            // TODO should we catch/throw a "parser" exception?
            String keyValue = current.get(FIELDS_KEY).get(KEY_OPTION).get(FIELD_VALUE).asText();
            Key key = new Key(keyValue);
            event = new KeyPressed(key);
        } else if (opcode.equals(event_whenthisspriteclicked)) {
            event = new Clicked();
        } else if (opcode.equals(event_whenbroadcastreceived)) {
            JsonNode fields = current.get(FIELDS_KEY);
            String msgValue = fields.get(BCAST_OPTION).get(FIELD_VALUE).asText();
            Message msg =
                new Message(msgValue); // TODO should we reference the previously parsed broadcast?
            event = new ReceptionOfMessage(msg);
        } else if (opcode.equals(control_start_as_clone)) {
            event = new StartedAsClone();
        } else if (opcode.equals(event_whengreaterthan)) {

            String variableValue = current.get(FIELDS_KEY).get(VARIABLE_MENU).get(0).asText();
            //TODO do I need a variable parser here?
            Identifier var = new Identifier(variableValue);

            JsonNode jsonNode = current.get(INPUTS).get(VARIABLE_MENU).get(Constants.POS_DATA_ARRAY);
            NumExpr fieldValue = ExpressionParser.parseNumExpr(jsonNode);

            event = new VariableAboveValue(var, fieldValue);
        } else if (opcode.equals(event_whenbackdropswitchesto)) {
            JsonNode fields = current.get(FIELDS_KEY);
            JsonNode backdropArray = fields.get(BACKDROP);
            String backdropName = backdropArray.get(FIELD_VALUE).asText();
            Identifier id = new Identifier(backdropName);
            event = new BackdropSwitchTo(id);
        } else {
            throw new IllegalStateException("EventBlock with opcode " + opcode + " was not parsed");
        }

        return event;
    }
}
