package scratch.structure.ast.stack;

import scratch.structure.ast.visitor.BlockVisitor;

public class HideBlock extends StackBlock {

    public HideBlock(String opcode, boolean shadow, boolean topLevel) {
        super(opcode, shadow, topLevel);
    }

    public HideBlock(String opcode, boolean shadow, boolean topLevel, int x, int y) {
        super(opcode, shadow, topLevel, x, y);
    }

    @Override
    public void accept(BlockVisitor visitor) {
        visitor.visit(this);
    }
}
