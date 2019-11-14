package scratch.newast.parser;

import static scratch.newast.Constants.FIELDS_KEY;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Preconditions;
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

        //TODO this may not work for all blocks, needs testing
        String blockMenuID = current.get(Constants.INPUTS_KEY).get(0).get(Constants.POS_INPUT_VALUE).asText();
        JsonNode menu = allBlocks.get(blockMenuID);
        String elementName = menu.get(FIELDS_KEY).get(0).get(0).asText();

        StandardElemChoice standardElemChoice = StandardElemChoice.valueOf((elementName.split(" ")[0]));
        switch (standardElemChoice) {
            case random:
                return new Random();
            case next:
                return new Next();
            case previous:
                return new Prev();
            default:
                return new WithId(new Identifier(elementName));
        }
    }

    private enum StandardElemChoice {
        random, next, previous
    }
}
