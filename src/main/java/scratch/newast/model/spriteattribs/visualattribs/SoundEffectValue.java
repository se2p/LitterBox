package scratch.newast.model.spriteattribs.visualattribs;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;
import scratch.newast.model.soundeffect.SoundEffect;

public class SoundEffectValue implements VisualAttribs {
    private final SoundEffect effect;
    private final ImmutableList<ASTNode> children;

    public SoundEffectValue(SoundEffect effect) {
        this.effect = effect;
        children = ImmutableList.<ASTNode>builder().add(effect).build();
    }

    public SoundEffect getEffect() {
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