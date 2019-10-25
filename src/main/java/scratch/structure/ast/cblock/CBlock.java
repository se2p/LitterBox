package scratch.structure.ast.cblock;

import scratch.structure.ast.Extendable;
import scratch.structure.ast.ScratchBlock;
import scratch.structure.ast.Stackable;
import scratch.structure.ast.inputs.SubstackSlot;

public abstract class CBlock extends ScratchBlock implements Stackable {

    protected SubstackSlot substack = null; //First block of the substack
    protected Extendable parent = null;

    public CBlock(String opcode, String id, Boolean shadow, Boolean topLevel) {
        super(opcode, id);
        this.shadow = shadow;
        this.topLevel = topLevel;
    }

    public CBlock(String opcode, String id, Boolean shadow, Boolean topLevel, Integer x, Integer y) {
        super(opcode, id);
        this.shadow = shadow;
        this.topLevel = topLevel;
        this.x = x;
        this.y = y;
    }

    public SubstackSlot getSubstack() {
        return substack;
    }

    public void setSubstack(SubstackSlot substack) {
        this.substack = substack;
    }

    @Override
    public Extendable getParent() {
        return parent;
    }

    @Override
    public void setParent(Extendable parent) {
        this.parent = parent;
    }
}
