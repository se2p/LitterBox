package scratch.structure.ast.stack;

import scratch.structure.ast.visitor.BlockVisitor;

public class ChangeSizeByBlock extends SingleIntInputBlock {


    public ChangeSizeByBlock(String opcode, Boolean shadow, Boolean topLevel) {
        super(opcode, shadow, topLevel);
    }

    public ChangeSizeByBlock(String opcode, Boolean shadow, Boolean topLevel, Integer x, Integer y) {
        super(opcode, shadow, topLevel, x, y);
    }

    @Override
    public void accept(BlockVisitor visitor) {
        visitor.visit(this);
    }
}
