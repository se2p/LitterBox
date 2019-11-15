package scratch.newast.parser.attributes;

public enum GraphicEffect {
    COLOR, GHOST, BRIGHTNESS, WHIRL, FISHEYE, PIXELATE, MOSAIC;

    public static boolean contains(String opcode) {
        for (GraphicEffect value : GraphicEffect.values()) {
            if (value.name().equals(opcode)) {
                return true;
            }
        }
        return false;
    }
}
