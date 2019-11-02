package scratch.newast.model.statement;

import scratch.newast.model.expression.numexpression.NumExpr;
import scratch.newast.model.soundeffect.SoundEffect;

public class ChangeSoundEffectBy implements EntitySoundStmt {
    private SoundEffect effect;
    private NumExpr num;

    public ChangeSoundEffectBy(SoundEffect effect, NumExpr num) {
        this.effect = effect;
        this.num = num;
    }

    public SoundEffect getEffect() {
        return effect;
    }

    public void setEffect(SoundEffect effect) {
        this.effect = effect;
    }

    public NumExpr getNum() {
        return num;
    }

    public void setNum(NumExpr num) {
        this.num = num;
    }
}