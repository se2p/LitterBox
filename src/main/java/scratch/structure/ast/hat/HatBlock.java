package scratch.structure.ast.hat;

import scratch.structure.ast.Extendable;
import scratch.structure.ast.ScratchBlock;
import scratch.structure.ast.Stackable;

public abstract class HatBlock extends ScratchBlock implements Extendable {

    //    private Object[] inputs; //Todo: Make this more specific, once we have proper types for inputs
    //    private Object[] fields; //Todo: Make this more specific, once we have proper types for fields

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

    @Override
    public void setParent(Extendable basicBlock) {
        throw new RuntimeException(); // TODO find proper exception
    }
}
