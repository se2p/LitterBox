package scratch.structure.ast.stack;

import scratch.structure.ast.visitor.BlockVisitor;

public class ChangeXCoordinateByBlock extends ChangeCoordinateByBlock {

    public ChangeXCoordinateByBlock(String opcode, String id, Boolean shadow, Boolean topLevel) {
        super(opcode, id, shadow, topLevel);
    }

    public ChangeXCoordinateByBlock(String opcode, String id, Boolean shadow, Boolean topLevel, Integer x, Integer y) {
        super(opcode, id, shadow, topLevel, x, y);
    }

    @Override
    public void accept(BlockVisitor visitor) {
        visitor.visit(this);
    }
}
