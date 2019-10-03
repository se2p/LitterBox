package scratch.structure.ast.hat;

import scratch.structure.ast.BasicBlock;
import scratch.structure.ast.Extendable;
import scratch.structure.ast.Stackable;

public abstract class HatBlock extends BasicBlock implements Extendable {

    //    private Object[] inputs; //Todo: Make this more specific, once we have proper types for inputs
    //    private Object[] fields; //Todo: Make this more specific, once we have proper types for fields
    private boolean shadow;
    private boolean topLevel; //This most likely will always be true
    private int x;
    private int y;

    public HatBlock(String opcode, Stackable next, boolean shadow, boolean topLevel, int x, int y) {
        super(opcode, null, next); // Hat Blocks do not ever have parents
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
