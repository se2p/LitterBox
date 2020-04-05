package de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound;

import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public class ChangeSoundEffectBy extends AbstractNode implements ActorSoundStmt {
    private NumExpr value;
    private SoundEffect effect;

    public ChangeSoundEffectBy(SoundEffect effect, NumExpr value) {
        super(effect, value);
        this.value = value;
        this.effect = effect;
    }

    public NumExpr getValue() {
        return value;
    }

    public SoundEffect getEffect() {
        return effect;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }
}
