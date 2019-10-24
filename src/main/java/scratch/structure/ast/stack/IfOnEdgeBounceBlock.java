package scratch.structure.ast.stack;

import scratch.structure.ast.visitor.BlockVisitor;

public class IfOnEdgeBounceBlock extends StackBlock {

    public IfOnEdgeBounceBlock(String opcode, Boolean shadow, Boolean topLevel) {
        super(opcode, shadow, topLevel);
    }

    public IfOnEdgeBounceBlock(String opcode, Boolean shadow, Boolean topLevel, Integer x, Integer y) {
        super(opcode, shadow, topLevel, x, y);
    }

    @Override
    public void accept(BlockVisitor visitor) {
        visitor.visit(this);
    }
}
