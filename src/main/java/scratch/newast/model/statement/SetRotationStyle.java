package scratch.newast.model.statement;

import scratch.newast.model.rotationstyle.RotationStyle;

public class SetRotationStyle extends SpriteMotionStmt {
    private RotationStyle rotationStyle;

    public SetRotationStyle(RotationStyle rotationStyle) {
        this.rotationStyle = rotationStyle;
    }

    public RotationStyle getRotationStyle() {
        return rotationStyle;
    }

    public void setRotationStyle(RotationStyle rotationStyle) {
        this.rotationStyle = rotationStyle;
    }
}