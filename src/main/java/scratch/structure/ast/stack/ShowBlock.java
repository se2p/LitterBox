package scratch.structure.ast.stack;

import scratch.structure.ast.visitor.BlockVisitor;

public class ShowBlock extends StackBlock {

    public ShowBlock(String opcode, boolean shadow, boolean topLevel) {
        super(opcode, shadow, topLevel);
    }

    public ShowBlock(String opcode, boolean shadow, boolean topLevel, int x, int y) {
        super(opcode, shadow, topLevel, x, y);
    }

    @Override
    public void accept(BlockVisitor visitor) {
        visitor.visit(this);
    }
}
