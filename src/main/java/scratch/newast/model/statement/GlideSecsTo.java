package scratch.newast.model.statement;

import scratch.newast.model.expression.NumExpr;
import scratch.newast.model.position.Position;

public class GlideSecsTo extends SpriteMotionStmt {
    private NumExpr secs;
    private Position position;

    public GlideSecsTo(NumExpr secs, Position position) {
        this.secs = secs;
        this.position = position;
    }

    public NumExpr getSecs() {
        return secs;
    }

    public void setSecs(NumExpr secs) {
        this.secs = secs;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }
}