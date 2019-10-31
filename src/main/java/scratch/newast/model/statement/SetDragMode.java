package scratch.newast.model.statement;

import scratch.newast.model.dragmode.DragMode;

public class SetDragMode extends SpriteMotionStmt {
    private DragMode dragMode;

    public SetDragMode(DragMode dragMode) {
        this.dragMode = dragMode;
    }

    public DragMode getDragMode() {
        return dragMode;
    }

    public void setDragMode(DragMode dragMode) {
        this.dragMode = dragMode;
    }
}