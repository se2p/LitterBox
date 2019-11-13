package scratch.newast.model.statement.actorsound;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;
import scratch.newast.model.expression.num.NumExpr;
import scratch.newast.model.soundeffect.SoundEffect;

public class ChangeSoundEffectBy implements ActorSoundStmt {
    private final SoundEffect effect;
    private final NumExpr num;
    private final ImmutableList<ASTNode> children;

    public ChangeSoundEffectBy(SoundEffect effect, NumExpr num) {
        this.effect = effect;
        this.num = num;
        children = ImmutableList.<ASTNode>builder().add(effect).add(num).build();
    }

    public SoundEffect getEffect() {
        return effect;
    }

    public NumExpr getNum() {
        return num;
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