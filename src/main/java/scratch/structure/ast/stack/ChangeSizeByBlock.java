package scratch.structure.ast.stack;

import scratch.structure.ast.Extendable;
import scratch.structure.ast.Stackable;
import scratch.structure.ast.visitor.BlockVisitor;

public class ChangeSizeByBlock extends SingleIntInputBlock {

    public ChangeSizeByBlock(String opcode, Stackable next, Extendable parent, Boolean shadow, Boolean topLevel) {
        super(opcode, next, parent, shadow, topLevel);
    }

    public ChangeSizeByBlock(String opcode, Stackable next, Extendable parent, Boolean shadow, Boolean topLevel, Integer x, Integer y) {
        super(opcode, next, parent, shadow, topLevel, x, y);
    }

    @Override
    public void accept(BlockVisitor visitor) {
        visitor.visit(this);
    }
}
