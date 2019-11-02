package scratch.newast.model.expression.bool;

import scratch.newast.model.touchable.Touchable;

public class Touching implements BoolExpr {
    private Touchable touchable;

    public Touching(Touchable touchable) {
        this.touchable = touchable;
    }

    public Touchable getTouchable() {
        return touchable;
    }

    public void setTouchable(Touchable touchable) {
        this.touchable = touchable;
    }
}