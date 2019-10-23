package scratch.structure.ast.cblock;

import scratch.structure.ast.Extendable;
import scratch.structure.ast.ScriptBodyBlock;
import scratch.structure.ast.Stackable;
import scratch.structure.ast.inputs.Slot;
import scratch.structure.ast.visitor.BlockVisitor;

public class RepeatBlock extends CBlock implements Extendable, Stackable {

    private Slot slot; //Slot into which an input can be inserted

    public RepeatBlock(String opcode, Extendable parent, Stackable next, ScriptBodyBlock substack, Boolean shadow, Boolean topLevel) {
        super(opcode, parent, next, substack, shadow, topLevel);
    }

    public RepeatBlock(String opcode, Extendable parent, Stackable next, ScriptBodyBlock substack, Boolean shadow, Boolean topLevel, Integer x, Integer y) {
        super(opcode, parent, next, substack, shadow, topLevel, x, y);
    }

    @Override
    public void accept(BlockVisitor visitor) {
        visitor.visit(this);
    }

    public Slot getSlot() {
        return slot;
    }

    public void setSlot(Slot slot) {
        this.slot = slot;
    }
}
