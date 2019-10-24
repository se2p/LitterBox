package scratch.structure.ast.cap;

import scratch.structure.ast.Extendable;
import scratch.structure.ast.visitor.BlockVisitor;

public class DeleteCloneBlock extends CapBlock {

    public DeleteCloneBlock(String opcode, Extendable parent, Boolean shadow, Boolean topLevel, Integer x, Integer y) {
        super(opcode, parent, shadow, topLevel, x, y);
    }

    @Override
    public void accept(BlockVisitor visitor) {
        visitor.visit(this);
    }
}
