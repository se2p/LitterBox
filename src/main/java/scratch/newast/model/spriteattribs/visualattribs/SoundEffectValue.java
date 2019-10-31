package scratch.newast.model.spriteattribs.visualattribs;

import scratch.newast.model.soundeffect.SoundEffect;
import scratch.newast.model.spriteattribs.VisualAttribs;

public class SoundEffectValue extends VisualAttribs {
    private SoundEffect effect;

    public SoundEffectValue(SoundEffect effect) {
        this.effect = effect;
    }

    public SoundEffect getEffect() {
        return effect;
    }

    public void setEffect(SoundEffect effect) {
        this.effect = effect;
    }
}