package scratch.structure.ast.stack;

import scratch.structure.ast.visitor.BlockVisitor;

public class ChangeVolumeByBlock extends SingleIntInputBlock {

    public ChangeVolumeByBlock(String opcode, String id, Boolean shadow, Boolean topLevel) {
        super(opcode, id, shadow, topLevel);
    }

    public ChangeVolumeByBlock(String opcode, String id, Boolean shadow, Boolean topLevel, Integer x, Integer y) {
        super(opcode, id, shadow, topLevel, x, y);
    }

    public void accept(BlockVisitor visitor) {
        visitor.visit(this);
    }
}
