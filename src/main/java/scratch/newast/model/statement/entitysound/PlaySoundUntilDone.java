package scratch.newast.model.statement.entitysound;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;
import scratch.newast.model.sound.Sound;

public class PlaySoundUntilDone implements EntitySoundStmt {
    private final Sound sound;
    private final ImmutableList<ASTNode> children;

    public PlaySoundUntilDone(Sound sound) {
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