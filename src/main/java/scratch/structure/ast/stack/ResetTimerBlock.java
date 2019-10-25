package scratch.structure.ast.stack;

import scratch.structure.ast.visitor.BlockVisitor;

public class ResetTimerBlock extends StackBlock {

    public ResetTimerBlock(String opcode, String id, boolean shadow, boolean topLevel) {
        super(opcode, id, shadow, topLevel);
    }

    public ResetTimerBlock(String opcode, String id, boolean shadow, boolean topLevel, int x, int y) {
        super(opcode, id, shadow, topLevel, x, y);
    }

    public void accept(BlockVisitor visitor) {
        visitor.visit(this);
    }
}
