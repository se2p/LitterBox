package scratch.newast.parser;

import static scratch.newast.Constants.FIELDS_KEY;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Preconditions;
import scratch.newast.Constants;
import scratch.newast.model.graphiceffect.Brightness;
import scratch.newast.model.graphiceffect.Color;
import scratch.newast.model.graphiceffect.Fisheye;
import scratch.newast.model.graphiceffect.Ghost;
import scratch.newast.model.graphiceffect.GraphicEffect;
import scratch.newast.model.graphiceffect.Mosaic;
import scratch.newast.model.graphiceffect.Pixelate;
import scratch.newast.model.graphiceffect.Whirl;

public class GraphicEffectParser {
    private static final String COLOR = "COLOR";
    private static final String GHOST = "GHOST";
    private static final String BRIGHTNESS = "BRIGHTNESS";
    private static final String WHIRL = "WHIRL";
    private static final String FISHEYE = "FISHEYE";
    private static final String PIXELATE = "PIXELATE";
    private static final String MOSAIC = "MOSAIC";

    private static final String EFFECTS_FIELD_KEY = "EFFECT";

    public static GraphicEffect parse(JsonNode jsonNode) {
        Preconditions.checkArgument(jsonNode.has(FIELDS_KEY));
        Preconditions.checkArgument(jsonNode.get(FIELDS_KEY).has(EFFECTS_FIELD_KEY));
        Preconditions.checkArgument(jsonNode.get(FIELDS_KEY).get(EFFECTS_FIELD_KEY).isArray());

        String effectName = jsonNode.get(FIELDS_KEY).get(EFFECTS_FIELD_KEY).get(Constants.FIELD_VALUE).asText();
        switch (effectName) {
            case COLOR:
                return new Color();
            case FISHEYE:
                return new Fisheye();
            case WHIRL:
                return new Whirl();
            case PIXELATE:
                return new Pixelate();
            case MOSAIC:
                return new Mosaic();
            case BRIGHTNESS:
                return new Brightness();
            case GHOST:
                return new Ghost();
            default:
                throw new RuntimeException("Not implemented yet");
        }
    }
}
