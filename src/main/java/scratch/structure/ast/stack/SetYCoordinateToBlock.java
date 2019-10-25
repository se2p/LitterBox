package scratch.structure.ast.stack;

import scratch.structure.ast.visitor.BlockVisitor;

public class SetYCoordinateToBlock extends SetCoordinateToBlock {

    public SetYCoordinateToBlock(String opcode, String id, Boolean shadow, Boolean topLevel) {
        super(opcode, id, shadow, topLevel);
    }

    public SetYCoordinateToBlock(String opcode, String id, Boolean shadow, Boolean topLevel, Integer x, Integer y) {
        super(opcode, id, shadow, topLevel, x, y);
    }

    @Override
    public void accept(BlockVisitor visitor) {
        visitor.visit(this);
    }
}
