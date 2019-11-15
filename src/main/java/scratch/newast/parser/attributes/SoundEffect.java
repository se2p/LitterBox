package scratch.newast.parser.attributes;

public enum SoundEffect {

     PAN, PITCH;

    public static boolean contains(String opcode) {
        for (SoundEffect value : SoundEffect.values()) {
            if (value.name().equals(opcode)) {
                return true;
            }
        }
        return false;
    }
}
