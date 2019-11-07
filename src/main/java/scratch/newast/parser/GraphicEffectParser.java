package scratch.newast.parser;

import scratch.newast.model.graphiceffect.Color;
import scratch.newast.model.graphiceffect.GraphicEffect;

public class GraphicEffectParser {

    public static GraphicEffect parse(String effectName) {
        //TODO make constant out of this
        if (effectName.equals("COLOR")) {
            return new Color();
        } else {
            throw new RuntimeException("Not implemented yet");
        }
    }

}
