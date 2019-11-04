package scratch.newast.parser;

import com.fasterxml.jackson.databind.JsonNode;
import scratch.newast.model.Key;
import scratch.newast.model.Message;
import scratch.newast.model.event.*;
import scratch.newast.model.variable.Identifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EventParser {

    public static final String EVENT_WHENFLAGCLICKED = "event_whenflagclicked";
    public static final String WHEN_GREEN_FLAG = "whenGreenFlag";
    public static final String EVENT_WHENKEYPRESSED = "event_whenkeypressed";
    public static final String WHEN_KEY_PRESSED = "whenKeyPressed";
    public static final String EVENT_WHENTHISSPRITECLICKED = "event_whenthisspriteclicked";
    public static final String WHEN_CLICKED = "whenClicked";
    public static final String EVENT_WHENBROADCASTRECEIVED = "event_whenbroadcastreceived";
    public static final String WHEN_I_RECEIVE = "whenIReceive";
    public static final String EVENT_WHENBACKDROPSWITCHESTO = "event_whenbackdropswitchesto";
    public static final String WHEN_SCENE_STARTS = "whenSceneStarts";
    public static final String CONTROL_START_AS_CLONE = "control_start_as_clone";
    public static final String WHEN_CLONED = "whenCloned";
    public static final String EVENT_WHENGREATERTHAN = "event_whengreaterthan";
    public static final String WHEN_SENSOR_GREATER_THAN = "whenSensorGreaterThan";
    /**
     * List of all opcodes of blocks which produce Events.
     * These are similar to Hat blocks, with the exception that a procedure_defintion is not an event.
     * Scratch 3 opcodes are followed by their equivalent in Scratch 2.
     */
    public static final List<String> eventOpcodes = new ArrayList<>(Arrays.asList(
            EVENT_WHENFLAGCLICKED,
            WHEN_GREEN_FLAG,
            EVENT_WHENKEYPRESSED,
            WHEN_KEY_PRESSED,
            EVENT_WHENTHISSPRITECLICKED,
            WHEN_CLICKED,
            EVENT_WHENBROADCASTRECEIVED,
            WHEN_I_RECEIVE,
            EVENT_WHENBACKDROPSWITCHESTO,
            WHEN_SCENE_STARTS,
            CONTROL_START_AS_CLONE,
            WHEN_CLONED,
            EVENT_WHENGREATERTHAN,
            WHEN_SENSOR_GREATER_THAN
    ));
    /**
     * These are constants we might need more often
     */
    public static String OPCODE = "opcode";
    public static String FIELDS = "fields";
    public static String INPUTS = "WHENGREATERTHANMENU";
    /****************************************************/

    public static String KEY_OPTION = "KEY_OPTION";
    public static String BCAST_OPTION = "BROADCAST_OPTION";
    public static String VARIABLE_MENU = "WHENGREATERTHANMENU";
    public static String VALUE = "WHENGREATERTHANMENU";
    public static String BACKDROP = "BACKDROP";

    public static Event parse(String blockID, JsonNode allBlocks) {
        if (!allBlocks.has(blockID)) {
            throw new IllegalArgumentException("Given blockID does not exist in blocks list.");
        }

        JsonNode current = allBlocks.get(blockID);
        String opcode = current.get(OPCODE).asText();
        if (!eventOpcodes.contains(opcode)) {
            throw new IllegalArgumentException("Given blockID does not point to an event block.");
        }

        Event event;
        if (opcode.equals(EVENT_WHENFLAGCLICKED) || opcode.equals(WHEN_GREEN_FLAG)) {
            event = new GreenFlag();
        } else if (opcode.equals(EVENT_WHENKEYPRESSED) || opcode.equals(WHEN_KEY_PRESSED)) {
            //TODO should we catch/throw a "parser" exception?
            String keyValue = current.get(FIELDS).get(KEY_OPTION).get(0).asText();
            Key key = new Key(keyValue);
            event = new KeyPressed(key);
        } else if (opcode.equals(EVENT_WHENTHISSPRITECLICKED) || opcode.equals(WHEN_CLICKED)) {
            event = new Clicked();
        } else if (opcode.equals(EVENT_WHENBROADCASTRECEIVED) || opcode.equals(WHEN_I_RECEIVE)) {
            String msgValue = current.get(FIELDS).get(BCAST_OPTION).get(0).asText();
            Message msg = new Message(msgValue); //TODO should we reference the previously parsed broadcast?
            event = new ReceptionOfMessage(msg);
        } else if (opcode.equals(CONTROL_START_AS_CLONE) || opcode.equals(WHEN_CLONED)) {
            event = new StartedAsClone();
        } else if (opcode.equals(EVENT_WHENGREATERTHAN) || opcode.equals(WHEN_SENSOR_GREATER_THAN)) {
            /*
            String variableValue = current.get(FIELDS).get(VARIABLE_MENU).get(0).asText();
            //TODO do I need a variable parser here?
            Identifier var = new Identifier(variableValue);

            JsonNode jsonNode = current.get(INPUTS).get(VALUE).get(1);
            NumExpr fieldValue = NumExprParser.parse(jsonNode);

            event = new VariableAboveValue(var, fieldValue);
            */
            throw new RuntimeException("Not implemented yet");
        } else if (opcode.equals(EVENT_WHENBACKDROPSWITCHESTO) || opcode.equals(WHEN_SCENE_STARTS)) {
            String backdropName = current.get(FIELDS).get(BACKDROP).get(0).asText();
            Identifier id = new Identifier(backdropName);
            event = new BackdropSwitchTo(id);
        } else {
            throw new IllegalStateException("EventBlock with opcode " + opcode + " was not parsed");
        }

        return event;
    }
}
