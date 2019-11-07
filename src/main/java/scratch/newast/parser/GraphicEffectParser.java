package scratch.newast.parser;

import scratch.newast.model.graphiceffect.*;

public class GraphicEffectParser {
    private static final String COLOR = "COLOR";
    private static final String GHOST = "GHOST";
    private static final String BRIGHTNESS = "BRIGHTNESS";
    private static final String WHIRL = "WHIRL";
    private static final String FISHEYE = "FISHEYE";
    private static final String PIXELATE = "PIXELATE";
    private static final String MOSAIC = "MOSAIC";


    public static GraphicEffect parse(String effectName) {
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
