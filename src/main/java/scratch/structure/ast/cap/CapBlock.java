package scratch.structure.ast.cap;

import scratch.structure.ast.Extendable;
import scratch.structure.ast.ScratchBlock;
import scratch.structure.ast.Stackable;

public abstract class CapBlock extends ScratchBlock implements Stackable {


    //    private Object[] inputs; //Todo: Make this more specific, once we have proper types for inputs
    //    private Object[] fields; //Todo: Make this more specific, once we have proper types for fields

    public CapBlock(String opcode, Extendable parent, boolean shadow, boolean topLevel, int x, int y) {
        super(opcode, parent, null);
        this.shadow = shadow;
        this.topLevel = topLevel;
        this.x = x;
        this.y = y;
    }

    public boolean isShadow() {
        return shadow;
    }

    public void setShadow(boolean shadow) {
        this.shadow = shadow;
    }

    public boolean isTopLevel() {
        return topLevel;
    }

    public void setTopLevel(boolean topLevel) {
        this.topLevel = topLevel;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
}
