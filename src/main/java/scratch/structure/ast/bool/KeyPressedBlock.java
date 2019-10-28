package scratch.structure.ast.bool;

import scratch.structure.ast.inputs.Slot;
import scratch.structure.ast.visitor.BlockVisitor;

public class KeyPressedBlock extends BooleanBlock {
    Slot keyOption;

    public KeyPressedBlock(String opcode, String id, Boolean shadow, Boolean topLevel) {
        super(opcode, id, shadow, topLevel);
    }

    public KeyPressedBlock(String opcode, String id, Boolean shadow, Boolean topLevel, Integer x, Integer y) {
        super(opcode, id, shadow, topLevel, x, y);
    }

    public Slot getKeyOption() {
        return keyOption;
    }

    public void setKeyOption(Slot keyOption) {
        this.keyOption = keyOption;
    }

    @Override
    public String toString() {
        return  keyOption + ", [" + x + "," + y + "]";
    }

    @Override
    public void accept(BlockVisitor visitor) {
        visitor.visit(this);
    }
}
