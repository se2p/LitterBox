package scratch.structure.ast.cblock;

import scratch.structure.ast.Extendable;
import scratch.structure.ast.ScratchBlock;
import scratch.structure.ast.Stackable;
import scratch.structure.ast.inputs.SubstackSlot;

public abstract class CBlock extends ScratchBlock  {

    protected SubstackSlot substack; //First block of the substack // TODO: Should we use a slot for this?

    public CBlock(String opcode, Extendable parent, Stackable next, SubstackSlot substack, Boolean shadow, Boolean topLevel) {
        super(opcode, parent, next);
        this.shadow = shadow;
        this.topLevel = topLevel;
        this.substack = substack;
    }

    public CBlock(String opcode, Extendable parent, Stackable next, SubstackSlot substack, Boolean shadow, Boolean topLevel, Integer x, Integer y) {
        super(opcode, parent, next);
        this.shadow = shadow;
        this.topLevel = topLevel;
        this.x = x;
        this.y = y;
        this.substack = substack;
    }

    public SubstackSlot getSubstack() {
        return substack;
    }

    public void setSubstack(SubstackSlot substack) {
        this.substack = substack;
    }
}
