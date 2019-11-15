package scratch.newast.parser;

import static scratch.newast.Constants.FIELDS_KEY;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.List;
import scratch.newast.Constants;
import scratch.newast.model.elementchoice.ElementChoice;
import scratch.newast.model.elementchoice.Next;
import scratch.newast.model.elementchoice.Prev;
import scratch.newast.model.elementchoice.Random;
import scratch.newast.model.elementchoice.WithId;
import scratch.newast.model.variable.Identifier;

public class ElementChoiceParser {

    public static ElementChoice parse(JsonNode current, JsonNode allBlocks) {
        Preconditions.checkNotNull(current);
        Preconditions.checkNotNull(allBlocks);

        //Make a list of all elements in inputs
        List<JsonNode> inputsList = new ArrayList<>();
        current.get(Constants.INPUTS_KEY).elements().forEachRemaining(inputsList::add);

        String blockMenuID = inputsList.get(0).get(Constants.POS_INPUT_VALUE).asText();
        JsonNode menu = allBlocks.get(blockMenuID);

        List<JsonNode> fieldsList = new ArrayList<>();
        menu.get(FIELDS_KEY).elements().forEachRemaining(fieldsList::add);
        String elementName = fieldsList.get(0).get(0).asText();

        String elemKey = elementName.split(" ")[0];
        if (!StandardElemChoice.contains(elemKey)) {
            return new WithId(new Identifier(elementName));
        }

        StandardElemChoice standardElemChoice = StandardElemChoice.valueOf(elemKey);
        switch (standardElemChoice) {
            case random:
                return new Random();
            case next:
                return new Next();
            case previous:
                return new Prev();
            default:
                throw new RuntimeException("No implementation for " + standardElemChoice);
        }
    }

    private enum StandardElemChoice {
        random, next, previous;

        public static boolean contains(String opcode) {
            for (StandardElemChoice value : StandardElemChoice.values()) {
                if (value.name().equals(opcode)) {
                    return true;
                }
            }
            return false;
        }
    }
}
