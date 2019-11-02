package scratch.newast.model.statement;

import scratch.newast.model.expression.num.NumExpr;
import scratch.newast.model.graphiceffect.GraphicEffect;

public class SetEffectTo implements EntityLookStmt {
    private GraphicEffect effect;
    private NumExpr num;

    public SetEffectTo(GraphicEffect effect, NumExpr num) {
        this.effect = effect;
        this.num = num;
    }

    public GraphicEffect getEffect() {
        return effect;
    }

    public void setEffect(GraphicEffect effect) {
        this.effect = effect;
    }

    public NumExpr getNum() {
        return num;
    }

    public void setNum(NumExpr num) {
        this.num = num;
    }
}