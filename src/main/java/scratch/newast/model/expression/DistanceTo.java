package scratch.newast.model.expression;

import scratch.newast.model.position.Position;

public class DistanceTo extends NumExpr {
    private Position position;

    public DistanceTo(Position position) {
        this.position = position;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }
}