package scratch.newast.model.statement;

import scratch.newast.model.position.Position;

public class GoToPos implements SpriteMotionStmt {
    private Position position;

    public GoToPos(Position position) {
        this.position = position;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }
}