package de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion;

import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public class SetDragMode extends AbstractNode implements SpriteMotionStmt {
    private DragMode drag;

    public SetDragMode(DragMode drag) {
        super(drag);
        this.drag = drag;
    }

    public DragMode getDrag() {
        return drag;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }

}
