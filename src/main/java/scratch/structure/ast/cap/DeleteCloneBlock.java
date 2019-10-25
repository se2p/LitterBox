package scratch.structure.ast.cap;

import scratch.structure.ast.visitor.BlockVisitor;

public class DeleteCloneBlock extends CapBlock {

    public DeleteCloneBlock(String opcode, String id, Boolean shadow, Boolean topLevel) {
        super(opcode, id, shadow, topLevel);
    }

    public DeleteCloneBlock(String opcode, String id, Boolean shadow, Boolean topLevel, Integer x, Integer y) {
        super(opcode, id, shadow, topLevel, x, y);
    }

    @Override
    public void accept(BlockVisitor visitor) {
        visitor.visit(this);
    }
}
