package scratch.structure.ast.stack;

import scratch.structure.ast.visitor.BlockVisitor;

public class NextBackdropBlock extends StackBlock {

    public NextBackdropBlock(String opcode, String id, boolean shadow, boolean topLevel) {
        super(opcode, id, shadow, topLevel);
    }

    public NextBackdropBlock(String opcode, String id, boolean shadow, boolean topLevel, int x, int y) {
        super(opcode, id, shadow, topLevel, x, y);
    }

    public void accept(BlockVisitor visitor) {
        visitor.visit(this);
    }
}
