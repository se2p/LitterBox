package scratch.structure.ast.stack;

import scratch.structure.ast.Extendable;
import scratch.structure.ast.Stackable;
import scratch.structure.ast.visitor.BlockVisitor;

public class ResetTimerBlock extends StackBlock {

    public ResetTimerBlock(String opcode, Extendable parent, Stackable next, Boolean shadow, Boolean topLevel) {
        super(opcode, parent, next, shadow, topLevel);
    }

    public ResetTimerBlock(String opcode, Extendable parent, Stackable next, Boolean shadow, Boolean topLevel, Integer x, Integer y) {
        super(opcode, parent, next, shadow, topLevel, x, y);
    }

    @Override
    public void accept(BlockVisitor visitor) {
        visitor.visit(this);
    }
}
