package scratch.newast.model.statement.spritemotion;

import scratch.newast.model.position.Position;

public class PointTowards implements SpriteMotionStmt {
    private Position position;

    public PointTowards(Position position) {
        this.position = position;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }
}