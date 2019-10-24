package scratch.structure.ast.stack;

import scratch.structure.ast.visitor.BlockVisitor;

public class SetVolumeToBlock extends SingleIntInputBlock {
    public SetVolumeToBlock(String opcode, Boolean shadow, Boolean topLevel) {
        super(opcode, shadow, topLevel);
    }

    public SetVolumeToBlock(String opcode, Boolean shadow, Boolean topLevel, Integer x, Integer y) {
        super(opcode, shadow, topLevel, x, y);
    }

    @Override
    public void accept(BlockVisitor visitor) {
        visitor.visit(this);
    }
}
