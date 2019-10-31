package scratch.newast.model.expression;

import scratch.newast.model.Key;

public class IsKeyPressed extends BoolExpr {
    private Key key;

    public IsKeyPressed(Key key) {
        this.key = key;
    }

    public Key getKey() {
        return key;
    }

    public void setKey(Key key) {
        this.key = key;
    }
}