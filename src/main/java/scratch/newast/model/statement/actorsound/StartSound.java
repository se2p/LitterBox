package scratch.newast.model.statement.actorsound;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;
import scratch.newast.model.sound.Sound;

public class StartSound implements ActorSoundStmt {
    private final Sound sound;
    private final ImmutableList<ASTNode> children;

    public StartSound(Sound sound) {
        this.sound = sound;
        children = ImmutableList.<ASTNode>builder().add(sound).build();
    }

    public Sound getSound() {
        return sound;
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