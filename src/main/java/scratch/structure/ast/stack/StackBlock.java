package scratch.structure.ast.stack;

import scratch.structure.ast.Extendable;
import scratch.structure.ast.ScriptBodyBlock;
import scratch.structure.ast.Stackable;

public abstract class StackBlock extends ScriptBodyBlock implements Stackable {


//    private Object[] inputs; //Todo: Make this more specific, once we have proper types for inputs
//    private Object[] fields; //Todo: Make this more specific, once we have proper types for fields
    private boolean shadow;
    private boolean topLevel;
    private int x;
    private int y;

    public StackBlock(String opcode, Extendable parent, Stackable next, boolean shadow, boolean topLevel) {
        super(opcode, parent, next);
        this.shadow = shadow;
        this.topLevel = topLevel;
    }

    public StackBlock(String opcode, Extendable parent, Stackable next, boolean shadow, boolean topLevel, int x, int y) {
        super(opcode, parent, next);
        this.shadow = shadow;
        this.topLevel = topLevel;
        this.x = x;
        this.y = y;
    }

    public boolean isShadow() {
        return shadow;
    }

    public boolean isTopLevel() {
        return topLevel;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

}
