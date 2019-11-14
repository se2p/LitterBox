package scratch.newast.parser;

import static scratch.newast.Constants.INPUTS_KEY;

import com.fasterxml.jackson.databind.JsonNode;
import scratch.newast.model.color.Color;
import scratch.newast.model.color.Rgba;
import scratch.newast.model.expression.num.Number;

public class ColorParser {

    public static Color parseColor(JsonNode current, int pos, JsonNode allBlocks) {
        String rgbCode = current.get(INPUTS_KEY).get(pos).get(1).asText();
        Long i;
        Float f;
        i = Long.parseLong(rgbCode.substring(1, 3), 16);
        f = Float.intBitsToFloat(i.intValue());
        Number rNumber = new Number(f);

        i = Long.parseLong(rgbCode.substring(3, 5), 16);
        f = Float.intBitsToFloat(i.intValue());
        Number gNumber = new Number(f);

        i = Long.parseLong(rgbCode.substring(5, 7), 16);
        f = Float.intBitsToFloat(i.intValue());
        Number bNumber = new Number(f);

        //TODO There is no alpha value?
        Number aNumber = new Number(0);

        return new Rgba(rNumber, gNumber, bNumber, aNumber);
    }

}
