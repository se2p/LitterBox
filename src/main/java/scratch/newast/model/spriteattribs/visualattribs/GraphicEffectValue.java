package scratch.newast.model.spriteattribs.visualattribs;

import scratch.newast.model.graphiceffect.GraphicEffect;

public class GraphicEffectValue implements VisualAttribs {
    private GraphicEffect effect;

    public GraphicEffectValue(GraphicEffect effect) {
        this.effect = effect;
    }

    public GraphicEffect getEffect() {
        return effect;
    }

    public void setEffect(GraphicEffect effect) {
        this.effect = effect;
    }
}