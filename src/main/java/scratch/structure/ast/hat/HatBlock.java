package scratch.structure.ast.hat;

import scratch.structure.ast.Extendable;
import scratch.structure.ast.ScratchBlock;
import scratch.structure.ast.Stackable;

public abstract class HatBlock extends ScratchBlock implements Extendable {

    Stackable next = null;

    public HatBlock(String opcode, boolean shadow, boolean topLevel, int x, int y) {
        super(opcode); // Hat Blocks do not ever have parents
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
    public Stackable getNext() {
        return next;
    }

    @Override
    public void setNext(Stackable next) {
        this.next = next;
    }
}
