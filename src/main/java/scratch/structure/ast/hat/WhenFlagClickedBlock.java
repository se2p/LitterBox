package scratch.structure.ast.hat;

import scratch.structure.ast.visitor.BlockVisitor;

public class WhenFlagClickedBlock extends HatBlock {

    public WhenFlagClickedBlock(String opcode, Boolean shadow, Boolean topLevel, Integer x, Integer y) {
        super(opcode, shadow, topLevel, x, y);
    }

    @Override
    public void accept(BlockVisitor visitor) {
        visitor.visit(this);
    }
}
