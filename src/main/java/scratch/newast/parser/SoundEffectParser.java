package scratch.newast.parser;

import scratch.newast.model.soundeffect.PanLeftRight;
import scratch.newast.model.soundeffect.Pitch;
import scratch.newast.model.soundeffect.SoundEffect;

public class SoundEffectParser {
    private static final String PAN = "PAN";
    private static final String PITCH = "PITCH";

    public static SoundEffect parse(String effectName) {
        switch (effectName) {
            case PAN:
                return new PanLeftRight();
            case PITCH:
                return new Pitch();
            default:
                throw new RuntimeException("Not implemented yet");
        }
    }
}
