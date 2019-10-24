package scratch.structure.ast.cap;

import scratch.structure.ast.visitor.BlockVisitor;

public class DeleteCloneBlock extends CapBlock {

    public DeleteCloneBlock(String opcode, Boolean shadow, Boolean topLevel) {
        super(opcode, shadow, topLevel, 0, 0);
    }

    public DeleteCloneBlock(String opcode, Boolean shadow, Boolean topLevel, Integer x, Integer y) {
        super(opcode, shadow, topLevel, x, y);
    }

    @Override
    public void accept(BlockVisitor visitor) {
        visitor.visit(this);
    }
}
