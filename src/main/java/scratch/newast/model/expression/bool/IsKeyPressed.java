package scratch.newast.model.expression.bool;

import scratch.newast.model.Key;

public class IsKeyPressed implements BoolExpr {
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