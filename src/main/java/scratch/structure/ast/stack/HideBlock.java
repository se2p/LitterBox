package scratch.structure.ast.stack;

import scratch.structure.ast.visitor.BlockVisitor;

public class HideBlock extends StackBlock {

    public HideBlock(String opcode, String id, boolean shadow, boolean topLevel) {
        super(opcode, id, shadow, topLevel);
    }

    public HideBlock(String opcode, String id, boolean shadow, boolean topLevel, int x, int y) {
        super(opcode, id, shadow, topLevel, x, y);
    }

    public void accept(BlockVisitor visitor) {
        visitor.visit(this);
    }
}
