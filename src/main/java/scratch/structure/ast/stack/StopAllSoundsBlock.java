package scratch.structure.ast.stack;

import scratch.structure.ast.visitor.BlockVisitor;

public class StopAllSoundsBlock extends StackBlock {

    public StopAllSoundsBlock(String opcode, String id, boolean shadow, boolean topLevel) {
        super(opcode, id, shadow, topLevel);
    }

    public StopAllSoundsBlock(String opcode, String id, boolean shadow, boolean topLevel, int x, int y) {
        super(opcode, id, shadow, topLevel, x, y);
    }

    @Override
    public void accept(BlockVisitor visitor) {
        visitor.visit(this);
    }
}
