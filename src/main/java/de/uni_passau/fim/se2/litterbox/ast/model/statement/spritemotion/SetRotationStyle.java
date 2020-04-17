package de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion;

import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public class SetRotationStyle extends AbstractNode implements SpriteMotionStmt {
    private RotationStyle rotation;

    public SetRotationStyle(RotationStyle rotation) {
        super(rotation);
        this.rotation = rotation;
    }

    public RotationStyle getRotation() {
        return rotation;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }
}
