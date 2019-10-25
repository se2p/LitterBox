package scratch.structure.ast.cblock;

import scratch.structure.ast.Extendable;
import scratch.structure.ast.Stackable;
import scratch.structure.ast.inputs.Slot;
import scratch.structure.ast.visitor.BlockVisitor;

public class RepeatBlock extends CBlock implements Extendable, Stackable {

    private Slot slot; //Slot into which an input can be inserted
    private Stackable next;

    public RepeatBlock(String opcode, String id, Boolean shadow, Boolean topLevel) {
        super(opcode, id, shadow, topLevel);
    }

    public RepeatBlock(String opcode, String id, Boolean shadow, Boolean topLevel, Integer x, Integer y) {
        super(opcode, id, shadow, topLevel, x, y);
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

    @Override
    public void setNext(Stackable next) {
        this.next = next;
    }

    @Override
    public Stackable getNext() {
        return next;
    }
}
