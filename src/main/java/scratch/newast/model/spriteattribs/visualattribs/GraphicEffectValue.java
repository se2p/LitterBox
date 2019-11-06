package scratch.newast.model.spriteattribs.visualattribs;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;
import scratch.newast.model.graphiceffect.GraphicEffect;

public class GraphicEffectValue implements VisualAttribs {
    private final GraphicEffect effect;
    private final ImmutableList<ASTNode> children;

    public GraphicEffectValue(GraphicEffect effect) {
        this.effect = effect;
        children = ImmutableList.<ASTNode>builder().add(effect).build();
    }

    public GraphicEffect getEffect() {
        return effect;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public ImmutableList<ASTNode> getChildren() {
        return children;
    }
}