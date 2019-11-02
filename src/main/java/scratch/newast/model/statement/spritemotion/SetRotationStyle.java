package scratch.newast.model.statement.spritemotion;

import scratch.newast.model.rotationstyle.RotationStyle;

public class SetRotationStyle implements SpriteMotionStmt {
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