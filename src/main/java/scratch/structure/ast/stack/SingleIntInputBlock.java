package scratch.structure.ast.stack;

import scratch.structure.ast.inputs.Slot;

public abstract class SingleIntInputBlock extends StackBlock {

    private Slot slot;

    public SingleIntInputBlock(String opcode, String id, Boolean shadow, Boolean topLevel) {
        super(opcode, id, shadow, topLevel);
    }

    public SingleIntInputBlock(String opcode, String id, Boolean shadow, Boolean topLevel, Integer x, Integer y) {
        super(opcode, id, shadow, topLevel, x, y);
    }

    public Slot getSlot() {
        return slot;
    }

    public void setSlot(Slot slot) {
        this.slot = slot;
    }
}
